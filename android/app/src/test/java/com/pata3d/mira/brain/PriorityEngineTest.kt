package com.pata3d.mira.brain

import com.pata3d.mira.brain.models.ContextBlockType
import com.pata3d.mira.brain.models.ContextSnapshot
import com.pata3d.mira.data.Tarefa
import org.junit.Assert.assertTrue
import org.junit.Test

class PriorityEngineTest {

    private val engine = PriorityEngine()

    private fun ctx(
        contexto: String = "oficina",
        availableMinutes: Int = 60,
        hasMachine: Boolean = false,
    ) = ContextSnapshot(
        nowMillis = 1_000_000_000_000L,
        blockType = ContextBlockType.WORKSHOP,
        availableMinutes = availableMinutes,
        isQuietHours = false,
        isWorkingTime = false,
        contexto = contexto,
        hasActiveMachineJob = hasMachine,
        pendingClientCount = 1,
        criticalDeadlineCount = 0,
    )

    private fun tarefa(
        titulo: String,
        categoria: String = "Geral",
        tempoEstimadoMin: Int? = null,
        contexto: String? = null,
        prazoEm: Long? = null,
        adiamentos: Int = 0,
    ) = Tarefa(
        id = titulo, titulo = titulo, categoria = categoria,
        tempoEstimadoMin = tempoEstimadoMin, contexto = contexto,
        prazoEm = prazoEm, adiamentos = adiamentos,
    )

    @Test fun cliente_rapido_vence_ideia() {
        val cliente = tarefa("Responder cliente sobre orçamento", "cliente", tempoEstimadoMin = 3)
        val ideia = tarefa("Ideia de novo produto", "ideia")
        val c = ctx()
        assertTrue(engine.score(cliente, c).finalScore > engine.score(ideia, c).finalScore)
    }

    @Test fun prazo_proximo_eleva_score() {
        val agora = 1_000_000_000_000L
        val comPrazo = tarefa("Entregar escudo", "producao", prazoEm = agora + 12 * 3_600_000L)
        val semPrazo = tarefa("Entregar escudo", "producao")
        val c = ctx().copy(nowMillis = agora)
        assertTrue(engine.score(comPrazo, c).finalScore > engine.score(semPrazo, c).finalScore)
    }

    @Test fun tarefa_de_oficina_penalizada_quando_fora() {
        val oficina = tarefa("Imprimir peça", "producao", contexto = "oficina")
        val emCasa = ctx(contexto = "fora")
        val naOficina = ctx(contexto = "oficina")
        assertTrue(engine.score(oficina, emCasa).finalScore < engine.score(oficina, naOficina).finalScore)
    }

    @Test fun muitos_adiamentos_sobem_score() {
        val travada = tarefa("Tarefa adiada", adiamentos = 4)
        val nova = tarefa("Tarefa adiada", adiamentos = 0)
        val c = ctx()
        assertTrue(engine.score(travada, c).finalScore > engine.score(nova, c).finalScore)
    }
}
