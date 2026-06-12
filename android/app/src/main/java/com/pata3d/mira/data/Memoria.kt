package com.pata3d.mira.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "memoria")
data class Memoria(
    @PrimaryKey val id: String,
    val memoryType: String = "behavior",
    val content: String = "",
    val confidence: String = "baixa",
    val isActive: Boolean = true,
    val origem: String = "manual",
    val criadaEm: Long = System.currentTimeMillis(),
    val atualizadaEm: Long = System.currentTimeMillis(),
)
