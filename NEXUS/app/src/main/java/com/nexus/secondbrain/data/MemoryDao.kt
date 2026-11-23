package com.nexus.secondbrain.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MemoryDao {
    @Query("SELECT * FROM memories ORDER BY createdAtMillis DESC")
    fun observeAll(): Flow<List<MemoryEntity>>

    @Insert
    suspend fun insert(memory: MemoryEntity): Long

    @Delete
    suspend fun delete(memory: MemoryEntity)

    @Query("DELETE FROM memories")
    suspend fun deleteAll()

    @Query("SELECT * FROM memories WHERE title LIKE '%' || :query || '%' OR body LIKE '%' || :query || '%' ORDER BY createdAtMillis DESC LIMIT :limit")
    suspend fun search(query: String, limit: Int = 10): List<MemoryEntity>
}
