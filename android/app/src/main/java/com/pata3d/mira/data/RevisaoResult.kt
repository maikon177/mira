package com.pata3d.mira.data

data class TarefaRevisada(
    val titulo: String,
    val categoria: String,
    val prioridade: String,
    val tempoEstimadoMinutos: Int?,
    val motivo: String,
    val proximaAcao: String,
    val alertaSugerido: String,
)

data class RevisaoResult(
    val tarefas: List<TarefaRevisada>,
    val proximaAcaoRecomendada: String,
    val motivoDaPrioridade: String,
    val observacao: String,
)
