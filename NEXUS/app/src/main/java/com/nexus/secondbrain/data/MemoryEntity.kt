package com.nexus.secondbrain.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "memories")
data class MemoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val body: String,
    val category: String = "QUICK NOTE",
    val createdAtMillis: Long = System.currentTimeMillis()
)
