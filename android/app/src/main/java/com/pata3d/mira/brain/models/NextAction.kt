package com.pata3d.mira.brain.models

data class NextAction(
    val taskId: String,
    val title: String,
    val microStep: String,
    val reason: String,
    val estimatedMinutes: Int,
    val riskLevel: String,      // NivelRisco.name
    val confidence: Float,      // 0..1
)
