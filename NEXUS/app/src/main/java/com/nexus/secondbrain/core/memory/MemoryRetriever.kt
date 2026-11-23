package com.nexus.secondbrain.core.memory

/**
 * Lightweight local retrieval baseline. Later versions can replace this with
 * Room FTS and optional semantic search while preserving the same call contract.
 */
data class RetrievedMemory(
    val title: String,
    val body: String,
    val updatedAtMillis: Long
) {
    fun asContextLine(): String = "$title: $body"
}

object MemoryRetriever {
    fun search(
        query: String,
        memories: List<RetrievedMemory>,
        limit: Int = 5
    ): List<RetrievedMemory> {
        val terms = query.lowercase()
            .split(Regex("[^\\p{L}\\p{N}]+"))
            .filter { it.length > 1 }
            .toSet()

        if (terms.isEmpty()) return emptyList()

        return memories.mapNotNull { memory ->
            val titleTerms = tokenize(memory.title)
            val bodyTerms = tokenize(memory.body)
            val score = terms.sumOf { term ->
                (if (term in titleTerms) 3 else 0) +
                    (if (term in bodyTerms) 1 else 0)
            }
            if (score == 0) null else memory to score
        }
            .sortedWith(
                compareByDescending<Pair<RetrievedMemory, Int>> { it.second }
                    .thenByDescending { it.first.updatedAtMillis }
            )
            .take(limit.coerceAtLeast(1))
            .map { it.first }
    }

    private fun tokenize(text: String): Set<String> =
        text.lowercase()
            .split(Regex("[^\\p{L}\\p{N}]+"))
            .filter { it.length > 1 }
            .toSet()
}
