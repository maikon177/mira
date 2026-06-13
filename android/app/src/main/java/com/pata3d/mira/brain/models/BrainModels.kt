package com.pata3d.mira.brain.models

enum class AiIntent {
    CREATE_TASK, UPDATE_TASK, SPLIT_TASK, SCHEDULE_TASK, CREATE_ALARM,
    SAVE_IDEA, COMPLETE_TASK, POSTPONE_TASK, ASK_CLARIFICATION,
    SUGGEST_NEXT_ACTION, SUGGEST_CLIENT_UPDATE, START_FOCUS,
    MOVE_TO_PARKING_LOT, REVIEW_DAY, PLAN_TOMORROW, UNKNOWN
}

enum class PermissionResult {
    EXECUTE_AUTOMATICALLY, REQUIRE_CONFIRMATION, BLOCKED
}

enum class MemoryCategory { BEHAVIOR, PREFERENCE, PATTERN }

data class MemoryFact(
    val text: String,
    val category: MemoryCategory,
    val confidence: Float,
)
