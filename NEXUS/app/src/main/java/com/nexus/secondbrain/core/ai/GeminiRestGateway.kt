package com.nexus.secondbrain.core.ai

import com.nexus.secondbrain.data.MemoryEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

/**
 * Direct Gemini API adapter for a personal prototype.
 * The API key is supplied at runtime and must never be committed to source control.
 * For a public release, move provider calls behind a trusted backend to protect the key.
 */
class GeminiRestGateway(
    private val apiKeyProvider: () -> String?,
    private val model: String = DEFAULT_MODEL
) {
    suspend fun answer(
        userMessage: String,
        relevantMemories: List<MemoryEntity>
    ): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val apiKey = apiKeyProvider()?.trim().orEmpty()
            require(apiKey.isNotEmpty()) {
                "Add your Gemini API key in NEXUS settings before using AI."
            }

            val memoryContext = relevantMemories.take(5).joinToString("\n") { memory ->
                val body = memory.body.take(700)
                "- ${memory.title}: $body"
            }.ifBlank { "No matching saved memories were found." }

            val prompt = """
                You are NEXUS, a practical personal second-brain assistant.
                Be accurate, concise, and distinguish known facts from uncertainty.
                Use the saved-memory excerpts only when relevant. They may be incomplete.
                Do not claim to have performed device actions.
                
                Relevant saved memory excerpts:
                $memoryContext
                
                User message:
                $userMessage
            """.trimIndent()

            val endpoint = URL(
                "https://generativelanguage.googleapis.com/v1beta/models/" +
                    "${URLEncoder.encode(model, "UTF-8")}:generateContent"
            )
            val connection = (endpoint.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = 15_000
                readTimeout = 30_000
                doOutput = true
                setRequestProperty("Content-Type", "application/json; charset=utf-8")
                setRequestProperty("x-goog-api-key", apiKey)
            }

            try {
                val request = JSONObject()
                    .put(
                        "contents",
                        JSONArray().put(
                            JSONObject()
                                .put("role", "user")
                                .put("parts", JSONArray().put(JSONObject().put("text", prompt)))
                        )
                    )
                    .put(
                        "generationConfig",
                        JSONObject().put("temperature", 0.4).put("maxOutputTokens", 1024)
                    )

                connection.outputStream.use { output ->
                    output.write(request.toString().toByteArray(Charsets.UTF_8))
                }

                val status = connection.responseCode
                val stream = if (status in 200..299) connection.inputStream else connection.errorStream
                val responseText = stream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }.orEmpty()
                val response = JSONObject(responseText)

                if (status !in 200..299) {
                    val message = response.optJSONObject("error")?.optString("message")
                        ?.takeIf { it.isNotBlank() }
                        ?: "Gemini request failed (HTTP $status). Check model access and API settings."
                    error(message)
                }

                val candidates = response.optJSONArray("candidates")
                    ?: error("Gemini returned no answer. Try a different prompt.")
                val parts = candidates.optJSONObject(0)
                    ?.optJSONObject("content")
                    ?.optJSONArray("parts")
                    ?: error("Gemini returned an empty answer.")
                val answer = (0 until parts.length()).mapNotNull { index ->
                    parts.optJSONObject(index)?.optString("text")?.takeIf { it.isNotBlank() }
                }.joinToString("\n").trim()

                answer.ifBlank { error("Gemini returned an empty answer.") }
            } finally {
                connection.disconnect()
            }
        }
    }

    companion object {
        // Model IDs can change. Keep this configurable in one place.
        const val DEFAULT_MODEL = "gemini-3.8-flash"
    }
}
