package com.pata3d.mira.brain

import com.pata3d.mira.brain.models.ContextSnapshot
import com.pata3d.mira.brain.models.NextAction
import com.pata3d.mira.brain.models.PriorityScore
import com.pata3d.mira.data.Tarefa

class NextActionEngine(private val priorityEngine: PriorityEngine) {

    /** Lista ordenada (maior score primeiro). Usada pelo hero e pelo "trocar". */
    fun rank(tarefas: List<Tarefa>, context: ContextSnapshot): List<NextAction> {
        val agora = context.nowMillis
        return tarefas
            .filter { t ->
                t.status !in listOf("concluida", "cancelada") &&
                !(t.status == "adiada" && t.adiarAte != null && t.adiarAte > agora)
            }
            .map { t -> t to priorityEngine.score(t, context) }
            .sortedByDescending { it.second.finalScore }
            .map { (t, score) -> toNextAction(t, score) }
    }

    fun decide(tarefas: List<Tarefa>, context: ContextSnapshot): NextAction? =
        rank(tarefas, context).firstOrNull()

    private fun toNextAction(tarefa: Tarefa, score: PriorityScore): NextAction {
        val microStep = tarefa.microPasso.ifBlank { tarefa.proximaAcao }
        val confidence = (score.finalScore.coerceAtLeast(0) / 150f).coerceIn(0f, 1f)
        return NextAction(
            taskId = tarefa.id,
            title = tarefa.titulo,
            microStep = microStep,
            reason = buildReason(score, tarefa),
            estimatedMinutes = tarefa.tempoEstimadoMin ?: 30,
            riskLevel = tarefa.nivelRisco,
            confidence = confidence,
        )
    }

    private fun buildReason(score: PriorityScore, tarefa: Tarefa): String = when {
        score.clientWeight > 0 && score.quickWinWeight > 0 ->
            "Cliente aguardando e é rápido (${tarefa.tempoEstimadoMin ?: "?"}min)."
        score.clientWeight > 0 && score.deadlineWeight >= 35 -> "Cliente aguardando e prazo crítico."
        score.clientWeight > 0 -> "Cliente aguardando."
        score.blockedProductionWeight >= 35 -> "Produção bloqueada."
        score.blockedProductionWeight > 0 -> "Destravar produção."
        score.deadlineWeight >= 35 -> "Prazo crítico."
        score.deadlineWeight >= 20 -> "Prazo próximo."
        score.snoozeWeight >= 36 -> "Adiada ${score.snoozeWeight / 12}× — hora de destravar."
        score.quickWinWeight > 0 -> "Rápida e de alto impacto."
        else -> "Maior prioridade agora."
    }
}
