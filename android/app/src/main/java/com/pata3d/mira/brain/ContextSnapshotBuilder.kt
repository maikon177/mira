package com.pata3d.mira.brain

import com.pata3d.mira.brain.models.ContextBlockType
import com.pata3d.mira.brain.models.ContextSnapshot
import com.pata3d.mira.data.MiraRepository

class ContextSnapshotBuilder(private val repo: MiraRepository) {

    suspend fun build(): ContextSnapshot {
        val ctx = repo.contextoAgora()
        val agora = System.currentTimeMillis()
        val abertas = repo.listarAbertas()

        val hasMachine = abertas.any { it.isTarefaMaquina && it.status == "fazendo" }
        val clientRegex = Regex("cliente|orcament|venda|financ", RegexOption.IGNORE_CASE)
        val clientCount = abertas.count { t ->
            clientRegex.containsMatchIn(t.categoria) || clientRegex.containsMatchIn(t.titulo)
        }
        val criticalCount = abertas.count { t ->
            t.prazoEm != null && t.prazoEm - agora in 0..24 * 3_600_000L
        }

        return ContextSnapshot(
            nowMillis = agora,
            blockType = mapBlockType(ctx.tipo, ctx.contexto),
            availableMinutes = ctx.tetoMinutos,
            isQuietHours = ctx.tipo == "dormir",
            isWorkingTime = ctx.tipo == "trabalho",
            contexto = ctx.contexto,
            hasActiveMachineJob = hasMachine,
            pendingClientCount = clientCount,
            criticalDeadlineCount = criticalCount,
        )
    }

    private fun mapBlockType(tipo: String, contexto: String): ContextBlockType = when (tipo) {
        "trabalho" -> ContextBlockType.WORK
        "pausa"    -> ContextBlockType.BREAK
        "dormir"   -> ContextBlockType.DO_NOT_DISTURB
        "livre"    -> if (contexto == "oficina") ContextBlockType.WORKSHOP else ContextBlockType.FREE_TIME
        else       -> ContextBlockType.HOME
    }
}
