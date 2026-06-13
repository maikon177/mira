package com.pata3d.mira.brain

import com.pata3d.mira.brain.models.ContextSnapshot
import com.pata3d.mira.brain.models.PriorityScore
import com.pata3d.mira.data.NivelRisco
import com.pata3d.mira.data.Tarefa
import com.pata3d.mira.domain.CalculadorRisco

class PriorityEngine {

    private val clientRegex  = Regex("cliente|orcament|venda|financ", RegexOption.IGNORE_CASE)
    private val moneyRegex   = Regex("orcament|venda|financ|nota.fiscal", RegexOption.IGNORE_CASE)
    private val machineRegex = Regex("manuten|bico|impressora|ender", RegexOption.IGNORE_CASE)
    private val ideaRegex    = Regex("ideia|pesquisa|estudo|criativ", RegexOption.IGNORE_CASE)

    fun score(tarefa: Tarefa, context: ContextSnapshot): PriorityScore {
        val agora = context.nowMillis
        val texto = "${tarefa.titulo} ${tarefa.categoria}"

        val clientWeight = if (clientRegex.containsMatchIn(texto)) 40 else 0

        val deadlineWeight = when {
            tarefa.prazoEm == null                  -> 0
            tarefa.prazoEm <= agora                 -> 50
            tarefa.prazoEm - agora < 86_400_000L    -> 35
            tarefa.prazoEm - agora < 259_200_000L   -> 20
            else                                    -> 5
        }

        val moneyWeight = if (moneyRegex.containsMatchIn(texto)) 30 else 0

        val blockedProductionWeight = when {
            machineRegex.containsMatchIn(texto) && context.hasActiveMachineJob -> 40
            tarefa.isTarefaMaquina && !context.hasActiveMachineJob && context.contexto == "oficina" -> 35
            else -> 0
        }

        val fits = tarefaCabeNoContexto(tarefa, context)
        val contextFitWeight = if (fits) 20 else 0
        val quickWinWeight = if ((tarefa.tempoEstimadoMin ?: 999) <= 15) 15 else 0
        val snoozeWeight = tarefa.adiamentos * 12

        val tempoTotal = listOfNotNull(
            tarefa.tempoPrepMin, tarefa.tempoMaquinaMin,
            tarefa.tempoSecagemMin, tarefa.tempoFinalMin, tarefa.tempoEstimadoMin
        ).sum()
        val riskWeight = if (tarefa.prazoEm != null && tempoTotal > 0) {
            when (CalculadorRisco.calcular(tarefa.prazoEm, tempoTotal, agora = agora)) {
                NivelRisco.CRITICO  -> 30
                NivelRisco.APERTADO -> 20
                NivelRisco.ATENCAO  -> 10
                else                -> 0
            }
        } else 0

        val vaguenessPenalty = if (tarefa.microPasso.isBlank() && tarefa.proximaAcao.isBlank()) -15 else 0
        val impossibleNowPenalty = if (!fits) -25 else 0
        val ideaPenalty = if (ideaRegex.containsMatchIn(texto) && tarefa.prazoEm == null) -20 else 0

        val finalScore = clientWeight + deadlineWeight + moneyWeight + blockedProductionWeight +
            contextFitWeight + quickWinWeight + snoozeWeight + riskWeight +
            vaguenessPenalty + impossibleNowPenalty + ideaPenalty

        return PriorityScore(
            taskId = tarefa.id,
            clientWeight = clientWeight, deadlineWeight = deadlineWeight, moneyWeight = moneyWeight,
            blockedProductionWeight = blockedProductionWeight, contextFitWeight = contextFitWeight,
            quickWinWeight = quickWinWeight, snoozeWeight = snoozeWeight, riskWeight = riskWeight,
            vaguenessPenalty = vaguenessPenalty, impossibleNowPenalty = impossibleNowPenalty,
            ideaPenalty = ideaPenalty, finalScore = finalScore,
        )
    }

    fun tarefaCabeNoContexto(tarefa: Tarefa, context: ContextSnapshot): Boolean {
        if (context.availableMinutes == 0) return false
        val ctxTarefa = tarefa.contexto ?: "qualquer"
        if (ctxTarefa == "qualquer") return true
        if (context.contexto == "fora") return false
        return ctxTarefa == context.contexto
    }
}
