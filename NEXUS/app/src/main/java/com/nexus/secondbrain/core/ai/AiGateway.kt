package com.nexus.secondbrain.core.ai

/**
 * Provider boundary for NEXUS. Keep the UI independent from a specific Gemini model
 * so the model ID/provider can be changed without rewriting the app.
 *
 * Implement this with a protected backend before shipping. A Gemini API key embedded
 * in an Android APK can be extracted; do not put a production secret in source code.
 */
interface AiGateway {
    suspend fun answer(
        userMessage: String,
        relevantMemories: List<String>
    ): Result<String>
}

/** Honest local fallback used until a real provider is configured. */
class UnconfiguredAiGateway : AiGateway {
    override suspend fun answer(
        userMessage: String,
        relevantMemories: List<String>
    ): Result<String> = Result.failure(
        IllegalStateException("AI provider is not configured. No message was sent.")
    )
}
