package com.pata3d.mira.data

data class AcaoIA(
    val tipo: String,
    val tarefaId: String? = null,
    val titulo: String? = null,
    val categoria: String? = null,
    val prioridade: String? = null,
    val tempoEstimadoMin: Int? = null,
    val proximaAcao: String? = null,
    val motivo: String? = null,
    val quando: String? = null,
    val etapas: List<NovaEtapa>? = null,
)

data class ChatResposta(
    val resposta: String,
    val acoes: List<AcaoIA>,
)

data class SnapshotEstado(
    val tarefasAbertas: List<Tarefa>,
    val memorias: List<Memoria>,
    val contextoAtual: String,
)
