package com.pata3d.mira.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "etapas_tarefa")
data class EtapaTarefa(
    @PrimaryKey val id: String,
    val tarefaId: String,
    val titulo: String,
    val ordem: Int,
    val tempoEstimadoMin: Int? = null,
    val tempoRealAcumuladoSeg: Long = 0,
    val cronometroIniciadoEm: Long? = null,
    val status: String = "aberta",
    val criadaEm: Long = System.currentTimeMillis(),
    val atualizadaEm: Long = System.currentTimeMillis(),
)

data class NovaEtapa(
    val titulo: String,
    val tempoEstimadoMin: Int? = null,
)
