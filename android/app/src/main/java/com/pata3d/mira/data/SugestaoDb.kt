package com.pata3d.mira.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "suggestions")
data class SugestaoDb(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: String,
    val title: String,
    val message: String,
    val relatedTaskId: String? = null,
    val urgency: Int,
    val usefulness: Int,
    val annoyanceRisk: Int,
    val status: String = "pending",
    val createdAt: Long = System.currentTimeMillis(),
    val expiresAt: Long = System.currentTimeMillis() + 3_600_000L,
)
