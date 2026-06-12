package com.pata3d.mira.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tarefas")
data class Tarefa(
    @PrimaryKey val id: String,
    val titulo: String,
    val categoria: String = "Geral",
    val prioridade: String = "Média",
    val tempoEstimadoMin: Int? = null,
    val tempoRealAcumuladoSeg: Long = 0,
    val cronometroIniciadoEm: Long? = null,
    val motivo: String = "",
    val proximaAcao: String = "",
    val status: String = "aberta",
    val revisadaIA: Boolean = false,
    val adiamentos: Int = 0,
    val adiarAte: Long? = null,
    val dataAgendada: Long? = null,
    val contexto: String? = null,
    val criadaEm: Long = System.currentTimeMillis(),
    val atualizadaEm: Long = System.currentTimeMillis(),
)
