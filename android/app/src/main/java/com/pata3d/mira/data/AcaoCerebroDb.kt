package com.pata3d.mira.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ai_actions")
data class AcaoCerebroDb(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val intent: String,
    val payloadJson: String = "{}",
    val requiresConfirmation: Boolean,
    val permissionResult: String,
    val status: String = "pending",
    val createdAt: Long = System.currentTimeMillis(),
    val executedAt: Long? = null,
)
