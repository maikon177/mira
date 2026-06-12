package com.pata3d.mira.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "historico")
data class Historico(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val tipo: String,
    val tarefaId: String? = null,
    val etapaId: String? = null,
    val detalhes: String? = null,
    val em: Long = System.currentTimeMillis(),
)
