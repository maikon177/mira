package com.pata3d.mira.brain

import com.pata3d.mira.brain.models.AiIntent
import com.pata3d.mira.brain.models.PermissionResult

enum class AutonomyMode { SAFE, ASSISTANT, FREE }

class AutonomyGate(private val getMode: () -> AutonomyMode) {

    fun canExecute(intent: AiIntent): PermissionResult {
        val mode = getMode()
        return when (intent) {
            AiIntent.SUGGEST_NEXT_ACTION, AiIntent.ASK_CLARIFICATION,
            AiIntent.SUGGEST_CLIENT_UPDATE, AiIntent.REVIEW_DAY, AiIntent.PLAN_TOMORROW ->
                if (mode == AutonomyMode.SAFE) PermissionResult.REQUIRE_CONFIRMATION
                else PermissionResult.EXECUTE_AUTOMATICALLY

            AiIntent.CREATE_TASK, AiIntent.SAVE_IDEA, AiIntent.SPLIT_TASK,
            AiIntent.START_FOCUS, AiIntent.MOVE_TO_PARKING_LOT -> when (mode) {
                AutonomyMode.SAFE -> PermissionResult.REQUIRE_CONFIRMATION
                else              -> PermissionResult.EXECUTE_AUTOMATICALLY
            }

            AiIntent.SCHEDULE_TASK, AiIntent.UPDATE_TASK -> when (mode) {
                AutonomyMode.FREE -> PermissionResult.EXECUTE_AUTOMATICALLY
                else              -> PermissionResult.REQUIRE_CONFIRMATION
            }

            AiIntent.CREATE_ALARM, AiIntent.COMPLETE_TASK, AiIntent.POSTPONE_TASK ->
                PermissionResult.REQUIRE_CONFIRMATION

            AiIntent.UNKNOWN -> PermissionResult.REQUIRE_CONFIRMATION
        }
    }
}
