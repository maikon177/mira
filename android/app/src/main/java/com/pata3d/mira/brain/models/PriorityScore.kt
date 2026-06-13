package com.pata3d.mira.brain.models

data class PriorityScore(
    val taskId: String,
    val clientWeight: Int,
    val deadlineWeight: Int,
    val moneyWeight: Int,
    val blockedProductionWeight: Int,
    val contextFitWeight: Int,
    val quickWinWeight: Int,
    val snoozeWeight: Int,
    val riskWeight: Int,
    val vaguenessPenalty: Int,
    val impossibleNowPenalty: Int,
    val ideaPenalty: Int,
    val finalScore: Int,
)
