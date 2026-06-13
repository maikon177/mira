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

    // Tipo e alerta
    val tipoTarefa: String = TipoTarefa.SIMPLES.name,
    val tipoAlerta: String = TipoAlerta.NENHUM.name,
    val nivelRisco: String = NivelRisco.NENHUM.name,
    val contextoNecessario: String = ContextoNecessario.QUALQUER.name,

    // Timestamps
    val prazoEm: Long? = null,
    val compromissoEm: Long? = null,
    val lembreteEm: Long? = null,
    val alarmeEm: Long? = null,
    val inicioAgendadoEm: Long? = null,

    // Proteção
    val protegerPrazo: Boolean = false,
    val microPasso: String = "",
    val ultimaNotificacaoEm: Long? = null,

    // Rastreamento
    val iniciadaEm: Long? = null,
    val concluidaEm: Long? = null,

    // Máquina/produção
    val isTarefaMaquina: Boolean = false,
    val tempoPrepMin: Int? = null,
    val tempoMaquinaMin: Int? = null,
    val tempoSecagemMin: Int? = null,
    val tempoFinalMin: Int? = null,
)
