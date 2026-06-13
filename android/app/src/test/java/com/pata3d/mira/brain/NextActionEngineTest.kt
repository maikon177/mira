package com.pata3d.mira.brain

import com.pata3d.mira.brain.models.ContextBlockType
import com.pata3d.mira.brain.models.ContextSnapshot
import com.pata3d.mira.data.Tarefa
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NextActionEngineTest {

    private val engine = NextActionEngine(PriorityEngine())

    private fun ctx() = ContextSnapshot(
        nowMillis = 1_000_000_000_000L, blockType = ContextBlockType.WORKSHOP,
        availableMinutes = 60, isQuietHours = false, isWorkingTime = false,
        contexto = "oficina", hasActiveMachineJob = false,
        pendingClientCount = 1, criticalDeadlineCount = 0,
    )

    private fun tarefa(id: String, titulo: String, cat: String = "Geral", tempo: Int? = null) =
        Tarefa(id = id, titulo = titulo, categoria = cat, tempoEstimadoMin = tempo)

    @Test fun caso1_escolhe_cliente_rapido() {
        val tarefas = listOf(
            tarefa("a", "Pintar peça", "producao", tempo = 60),
            tarefa("b", "Responder cliente sobre orçamento", "cliente", tempo = 3),
            tarefa("c", "Pesquisar logo nova", "ideia", tempo = 30),
        )
        val action = engine.decide(tarefas, ctx())!!
        assertEquals("b", action.taskId)
        assertTrue(action.reason.isNotBlank())
    }

    @Test fun ignora_concluidas_e_canceladas() {
        val tarefas = listOf(
            tarefa("a", "Tarefa viva"),
            tarefa("b", "Tarefa morta").copy(status = "concluida"),
        )
        assertEquals("a", engine.decide(tarefas, ctx())!!.taskId)
    }

    @Test fun rank_ordena_decrescente() {
        val tarefas = listOf(
            tarefa("a", "Ideia qualquer", "ideia"),
            tarefa("b", "Responder cliente", "cliente", tempo = 5),
        )
        val rank = engine.rank(tarefas, ctx())
        assertEquals("b", rank.first().taskId)
    }

    @Test fun lista_vazia_retorna_null() {
        assertEquals(null, engine.decide(emptyList(), ctx()))
    }
}
