package com.pata3d.mira.domain

import com.pata3d.mira.data.Memoria
import com.pata3d.mira.data.Tarefa
import java.util.Calendar

enum class TipoSugestao { ENCAIXE, DESTRAVAR, PLANEJAR }

data class Sugestao(
    val tipo: TipoSugestao,
    val tarefa: Tarefa,
    val texto: String,
    val ctaPrimario: String,
)

object Sugestoes {

    /**
     * Gera até [max] sugestões, no máximo uma de cada tipo, considerando o contexto atual.
     * Determinístico e local — não usa IA.
     */
    fun gerar(
        tarefas: List<Tarefa>,
        contexto: ContextoAtual,
        memorias: List<Memoria> = emptyList(),
        max: Int = 3,
        destravarAtivo: Boolean = true,
        dispensadas: Set<String> = emptySet(),
        agora: Long = System.currentTimeMillis(),
    ): List<Sugestao> {
        val abertas = tarefas.filter {
            it.status !in listOf("concluida", "cancelada") && it.id !in dispensadas
        }
        if (abertas.isEmpty()) return emptyList()

        val resultado = mutableListOf<Sugestao>()

        // 1. ENCAIXE — tarefa curta que cabe na janela atual e no contexto.
        if (contexto.tetoMinutos > 0) {
            val encaixe = abertas
                .filter { cabeNoContexto(it, contexto) }
                .filter { (it.tempoEstimadoMin ?: 30) <= minOf(contexto.tetoMinutos, 15) }
                .maxByOrNull { scoreTarefa(it, memorias) }
            if (encaixe != null) {
                resultado += Sugestao(
                    TipoSugestao.ENCAIXE,
                    encaixe,
                    "Cabe agora: ${encaixe.titulo}" +
                        (encaixe.tempoEstimadoMin?.let { " ($it min)" } ?: ""),
                    "Fazer agora",
                )
            }
        }

        // 2. DESTRAVAR — a mais adiada (>= 3 adiamentos).
        if (destravarAtivo) {
            val travada = abertas
                .filter { it.adiamentos >= 3 && it.id !in resultado.map { r -> r.tarefa.id } }
                .maxByOrNull { it.adiamentos }
            if (travada != null) {
                resultado += Sugestao(
                    TipoSugestao.DESTRAVAR,
                    travada,
                    "Adiada ${travada.adiamentos}×: ${travada.titulo}. Quebrar em passo menor?",
                    "Abrir",
                )
            }
        }

        // 3. PLANEJAR — tarefa sem data com score alto.
        val planejar = abertas
            .filter { it.dataAgendada == null && it.id !in resultado.map { r -> r.tarefa.id } }
            .maxByOrNull { scoreTarefa(it, memorias) }
        if (planejar != null) {
            resultado += Sugestao(
                TipoSugestao.PLANEJAR,
                planejar,
                "Sem data: ${planejar.titulo}",
                "Agendar",
            )
        }

        return resultado.take(max)
    }

    /** A tarefa pode ser feita no contexto atual (oficina/casa/fora)? */
    private fun cabeNoContexto(tarefa: Tarefa, contexto: ContextoAtual): Boolean {
        val ctxTarefa = tarefa.contexto ?: "qualquer"
        if (ctxTarefa == "qualquer" || contexto.contexto == "qualquer") return true
        // "fora" do trabalho só aceita tarefas marcadas como "qualquer".
        if (contexto.contexto == "fora") return ctxTarefa == "qualquer"
        return ctxTarefa == contexto.contexto
    }

    fun inicioDoDia(epoch: Long): Long = Calendar.getInstance().apply {
        timeInMillis = epoch
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    fun mesmoDia(a: Long, b: Long): Boolean = inicioDoDia(a) == inicioDoDia(b)
}
