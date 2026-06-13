package com.pata3d.mira.brain.models

enum class SuggestionType {
    CLIENT_UPDATE, DEADLINE_WARNING, QUICK_WIN, UNBLOCK_PRODUCTION,
    SPLIT_TASK, MENTAL_CLEANUP, IDEA_PARKING
}

data class Suggestion(
    val type: SuggestionType,
    val title: String,
    val message: String,
    val relatedTaskId: String?,
    val urgency: Int,       // 1-10
    val usefulness: Int,    // 1-10
    val annoyanceRisk: Int, // 1-10
    val requiresConfirmation: Boolean,
)
