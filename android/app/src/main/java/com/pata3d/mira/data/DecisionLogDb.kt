package com.pata3d.mira.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "decision_logs")
data class DecisionLogDb(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val selectedTaskId: String,
    val reason: String,
    val scoreJson: String = "{}",
    val contextJson: String = "{}",
    val createdAt: Long = System.currentTimeMillis(),
)
