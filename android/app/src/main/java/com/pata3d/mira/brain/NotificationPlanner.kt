package com.pata3d.mira.brain

import com.pata3d.mira.brain.models.ContextSnapshot
import com.pata3d.mira.brain.models.Suggestion

class NotificationPlanner {

    /** utilidade + urgência > risco de incomodar + THRESHOLD */
    fun shouldNotify(suggestion: Suggestion, context: ContextSnapshot): Boolean {
        if (context.isQuietHours) return false
        if (context.availableMinutes == 0) return false
        return suggestion.usefulness + suggestion.urgency > suggestion.annoyanceRisk + 5
    }

    fun pickBest(suggestions: List<Suggestion>, context: ContextSnapshot): Suggestion? =
        suggestions
            .filter { shouldNotify(it, context) }
            .maxByOrNull { it.usefulness + it.urgency - it.annoyanceRisk }
}
