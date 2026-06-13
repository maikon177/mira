package com.pata3d.mira.brain

import com.pata3d.mira.brain.models.AiIntent
import com.pata3d.mira.brain.models.PermissionResult
import org.junit.Assert.assertEquals
import org.junit.Test

class AutonomyGateTest {

    private fun gate(mode: AutonomyMode) = AutonomyGate { mode }

    @Test fun assistant_cria_tarefa_automaticamente() {
        assertEquals(PermissionResult.EXECUTE_AUTOMATICALLY,
            gate(AutonomyMode.ASSISTANT).canExecute(AiIntent.CREATE_TASK))
    }

    @Test fun assistant_pede_confirmacao_para_concluir() {
        assertEquals(PermissionResult.REQUIRE_CONFIRMATION,
            gate(AutonomyMode.ASSISTANT).canExecute(AiIntent.COMPLETE_TASK))
    }

    @Test fun safe_sempre_pede_confirmacao_para_criar() {
        assertEquals(PermissionResult.REQUIRE_CONFIRMATION,
            gate(AutonomyMode.SAFE).canExecute(AiIntent.CREATE_TASK))
    }

    @Test fun free_agenda_automaticamente_mas_confirma_alarme() {
        val g = gate(AutonomyMode.FREE)
        assertEquals(PermissionResult.EXECUTE_AUTOMATICALLY, g.canExecute(AiIntent.SCHEDULE_TASK))
        assertEquals(PermissionResult.REQUIRE_CONFIRMATION, g.canExecute(AiIntent.CREATE_ALARM))
    }
}
