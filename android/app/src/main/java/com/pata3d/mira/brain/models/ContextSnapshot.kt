package com.pata3d.mira.brain.models

enum class ContextBlockType {
    WORK, BREAK, COMMUTE, HOME, WORKSHOP, FREE_TIME, DO_NOT_DISTURB
}

data class ContextSnapshot(
    val nowMillis: Long,
    val blockType: ContextBlockType,
    val availableMinutes: Int,
    val isQuietHours: Boolean,
    val isWorkingTime: Boolean,
    val contexto: String,           // "casa" | "oficina" | "fora" | "qualquer"
    val hasActiveMachineJob: Boolean,
    val pendingClientCount: Int,
    val criticalDeadlineCount: Int,
)
