package com.pata3d.mira.brain

import com.pata3d.mira.brain.models.ContextBlockType
import com.pata3d.mira.brain.models.ContextSnapshot
import com.pata3d.mira.brain.models.Suggestion
import com.pata3d.mira.brain.models.SuggestionType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NotificationPlannerTest {

    private val planner = NotificationPlanner()

    private fun ctx(quiet: Boolean = false, minutes: Int = 30) = ContextSnapshot(
        nowMillis = 0, blockType = ContextBlockType.FREE_TIME, availableMinutes = minutes,
        isQuietHours = quiet, isWorkingTime = false, contexto = "oficina",
        hasActiveMachineJob = false, pendingClientCount = 0, criticalDeadlineCount = 0,
    )

    private fun sug(u: Int, urg: Int, annoy: Int) = Suggestion(
        SuggestionType.QUICK_WIN, "t", "m", null, urgency = urg, usefulness = u,
        annoyanceRisk = annoy, requiresConfirmation = false,
    )

    @Test fun util_e_urgente_notifica() {
        assertTrue(planner.shouldNotify(sug(u = 9, urg = 8, annoy = 2), ctx()))
    }

    @Test fun baixa_utilidade_nao_notifica() {
        assertFalse(planner.shouldNotify(sug(u = 3, urg = 2, annoy = 6), ctx()))
    }

    @Test fun horario_silencio_nunca_notifica() {
        assertFalse(planner.shouldNotify(sug(u = 9, urg = 9, annoy = 1), ctx(quiet = true)))
    }

    @Test fun pickBest_escolhe_maior_net() {
        val baixa = sug(u = 6, urg = 6, annoy = 3)
        val alta = sug(u = 9, urg = 9, annoy = 2)
        assertEquals(alta, planner.pickBest(listOf(baixa, alta), ctx()))
    }
}
