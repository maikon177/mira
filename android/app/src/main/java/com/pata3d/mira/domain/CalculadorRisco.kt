package com.pata3d.mira.domain

import com.pata3d.mira.data.NivelRisco

object CalculadorRisco {

    /**
     * Calcula NivelRisco com base em prazo, tempo total necessário e horário de sono.
     * Buffer = (minutosUteisRestantes - tempoTotalMin) / tempoTotalMin
     * CRITICO < 10% < APERTADO < 30% < ATENCAO < 70% < NORMAL
     */
    fun calcular(
        prazoEm: Long,
        tempoTotalMin: Int,
        silencioInicioMin: Int = 22 * 60,
        silencioFimMin: Int = 7 * 60,
        agora: Long = System.currentTimeMillis(),
    ): NivelRisco {
        if (prazoEm <= agora) return NivelRisco.CRITICO
        if (tempoTotalMin <= 0) return NivelRisco.NORMAL

        val minutosUteisRestantes = calcularMinutosUteis(agora, prazoEm, silencioInicioMin, silencioFimMin)
        val folga = minutosUteisRestantes - tempoTotalMin
        val buffer = folga.toDouble() / tempoTotalMin
        return when {
            buffer < 0.10 -> NivelRisco.CRITICO
            buffer < 0.30 -> NivelRisco.APERTADO
            buffer < 0.70 -> NivelRisco.ATENCAO
            else          -> NivelRisco.NORMAL
        }
    }

    /** Soma os tempos de produção em um total. Qualquer null conta como 0. */
    fun tempoTotalProducaoMin(
        prep: Int?,
        maquina: Int?,
        secagem: Int?,
        finalizacao: Int?,
        estimativa: Int?,
    ): Int = (prep ?: 0) + (maquina ?: 0) + (secagem ?: 0) + (finalizacao ?: 0) + (estimativa ?: 0)

    /** Intervalo de alerta recomendado (em minutos) para cada nível de risco. */
    fun intervaloAlertaMin(nivel: NivelRisco): Int = when (nivel) {
        NivelRisco.CRITICO   -> 15
        NivelRisco.APERTADO  -> 30
        NivelRisco.ATENCAO   -> 90
        NivelRisco.NORMAL    -> 120
        NivelRisco.NENHUM    -> Int.MAX_VALUE
    }

    private fun calcularMinutosUteis(
        agora: Long,
        prazoEm: Long,
        silencioInicioMin: Int,
        silencioFimMin: Int,
    ): Long {
        val minutosRestantes = (prazoEm - agora) / 60_000L
        if (minutosRestantes <= 0) return 0
        val minutosDormindoPorDia: Int = if (silencioFimMin > silencioInicioMin) {
            silencioFimMin - silencioInicioMin
        } else {
            24 * 60 - silencioInicioMin + silencioFimMin
        }
        val proporcaoDormindo = minutosDormindoPorDia.toDouble() / (24 * 60)
        return (minutosRestantes * (1.0 - proporcaoDormindo)).toLong()
    }
}
