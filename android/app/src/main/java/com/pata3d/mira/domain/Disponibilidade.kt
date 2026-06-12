package com.pata3d.mira.domain

import com.pata3d.mira.data.BlocoDisponibilidade
import java.util.Calendar

/** Contexto atual derivado do cronograma do usuário. */
data class ContextoAtual(
    val diaSemana: Int,        // 1=seg ... 7=dom
    val horaMin: Int,          // minutos desde meia-noite
    val tipo: String,          // "trabalho" | "pausa" | "livre" | "dormir" | "indefinido"
    val nivel: String,         // "indisponivel" | "baixa" | "media" | "alta"
    val contexto: String,      // "casa" | "oficina" | "fora" | "qualquer"
) {
    /** Maior tempo (min) de tarefa que cabe neste nível. */
    val tetoMinutos: Int
        get() = when (nivel) {
            "indisponivel" -> 0
            "baixa"        -> 5
            "media"        -> 20
            "alta"         -> 90
            else           -> 30
        }

    val rotuloAmigavel: String
        get() = when (tipo) {
            "trabalho" -> "Trabalho"
            "pausa"    -> "Pausa"
            "livre"    -> "Tempo livre"
            "dormir"   -> "Descanso"
            else       -> "Agora"
        }
}

object Disponibilidade {

    fun agora(blocos: List<BlocoDisponibilidade>, cal: Calendar = Calendar.getInstance()): ContextoAtual {
        val dia = isoDiaSemana(cal)
        val minutos = cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE)

        val bloco = blocos.firstOrNull {
            it.diaSemana == dia && minutos >= it.horaInicio && minutos < it.horaFim
        }

        return if (bloco != null) {
            ContextoAtual(dia, minutos, bloco.tipo, bloco.nivel, bloco.contexto)
        } else {
            // Sem bloco definido: assume disponibilidade média neutra.
            ContextoAtual(dia, minutos, "indefinido", "media", "qualquer")
        }
    }

    /** Cronograma padrão sugerido (docs/cronograma_contexto_disponibilidade.md). */
    fun cronogramaPadrao(): List<BlocoDisponibilidade> {
        val blocos = mutableListOf<BlocoDisponibilidade>()
        // Segunda a sexta (1..5)
        for (dia in 1..5) {
            blocos += BlocoDisponibilidade(diaSemana = dia, horaInicio = 7 * 60,  horaFim = 11 * 60,      tipo = "trabalho", nivel = "indisponivel", contexto = "fora")
            blocos += BlocoDisponibilidade(diaSemana = dia, horaInicio = 11 * 60, horaFim = 13 * 60,      tipo = "pausa",    nivel = "baixa",        contexto = "fora")
            blocos += BlocoDisponibilidade(diaSemana = dia, horaInicio = 13 * 60, horaFim = 17 * 60 + 30, tipo = "trabalho", nivel = "indisponivel", contexto = "fora")
            blocos += BlocoDisponibilidade(diaSemana = dia, horaInicio = 18 * 60, horaFim = 22 * 60,      tipo = "livre",    nivel = "alta",         contexto = "oficina")
            blocos += BlocoDisponibilidade(diaSemana = dia, horaInicio = 22 * 60, horaFim = 23 * 60 + 59, tipo = "dormir",   nivel = "indisponivel", contexto = "casa")
        }
        // Sábado (6)
        blocos += BlocoDisponibilidade(diaSemana = 6, horaInicio = 9 * 60,  horaFim = 18 * 60,      tipo = "livre",  nivel = "alta",  contexto = "oficina")
        blocos += BlocoDisponibilidade(diaSemana = 6, horaInicio = 18 * 60, horaFim = 19 * 60,      tipo = "livre",  nivel = "media", contexto = "casa")
        // Domingo (7)
        blocos += BlocoDisponibilidade(diaSemana = 7, horaInicio = 10 * 60, horaFim = 17 * 60,      tipo = "livre",  nivel = "media", contexto = "casa")
        blocos += BlocoDisponibilidade(diaSemana = 7, horaInicio = 17 * 60, horaFim = 18 * 60,      tipo = "livre",  nivel = "baixa", contexto = "casa")
        return blocos
    }

    private fun isoDiaSemana(cal: Calendar): Int =
        when (cal.get(Calendar.DAY_OF_WEEK)) {
            Calendar.MONDAY    -> 1
            Calendar.TUESDAY   -> 2
            Calendar.WEDNESDAY -> 3
            Calendar.THURSDAY  -> 4
            Calendar.FRIDAY    -> 5
            Calendar.SATURDAY  -> 6
            else               -> 7
        }
}
