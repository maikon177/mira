package com.pata3d.mira.brain

import com.pata3d.mira.brain.models.ContextSnapshot
import com.pata3d.mira.brain.models.Suggestion
import com.pata3d.mira.brain.models.SuggestionType
import com.pata3d.mira.data.Tarefa

class SuggestionEngine {

    private val clientRegex = Regex("cliente|orcament|venda", RegexOption.IGNORE_CASE)
    private val ideaRegex   = Regex("ideia|pesquisa|estudo", RegexOption.IGNORE_CASE)

    fun generate(tarefas: List<Tarefa>, context: ContextSnapshot): List<Suggestion> {
        val abertas = tarefas.filter { it.status !in listOf("concluida", "cancelada") }
        val result = mutableListOf<Suggestion>()

        abertas.firstOrNull { it.status == "fazendo" && clientRegex.containsMatchIn("${it.titulo} ${it.categoria}") }?.let { t ->
            result += Suggestion(SuggestionType.CLIENT_UPDATE, "Atualizar cliente",
                "\"${t.titulo}\" está em andamento. Que tal mandar uma mensagem curta?",
                t.id, urgency = 4, usefulness = 7, annoyanceRisk = 2, requiresConfirmation = true)
        }

        if (context.availableMinutes in 5..20) {
            abertas.filter { (it.tempoEstimadoMin ?: 999) <= context.availableMinutes }
                .maxByOrNull { it.adiamentos }?.let { t ->
                result += Suggestion(SuggestionType.QUICK_WIN, "Encaixe rápido",
                    "Você tem ${context.availableMinutes} min. Cabe: ${t.titulo}",
                    t.id, urgency = 5, usefulness = 8, annoyanceRisk = 3, requiresConfirmation = false)
            }
        }

        if (!context.hasActiveMachineJob) {
            abertas.firstOrNull { it.isTarefaMaquina && it.status != "fazendo" }?.let { t ->
                result += Suggestion(SuggestionType.UNBLOCK_PRODUCTION, "Produção parada",
                    "Impressora sem tarefa. Próximo passo: ${t.proximaAcao.ifBlank { t.titulo }}",
                    t.id, urgency = 8, usefulness = 9, annoyanceRisk = 2, requiresConfirmation = false)
            }
        }

        abertas.firstOrNull { it.adiamentos >= 3 && it.microPasso.isBlank() }?.let { t ->
            result += Suggestion(SuggestionType.SPLIT_TASK, "Tarefa grande",
                "\"${t.titulo}\" foi adiada ${t.adiamentos}×. Posso dividir em passos menores?",
                t.id, urgency = 3, usefulness = 7, annoyanceRisk = 4, requiresConfirmation = true)
        }

        val ideias = abertas.filter { ideaRegex.containsMatchIn("${it.titulo} ${it.categoria}") }
        if (ideias.size >= 3 && context.pendingClientCount > 0) {
            result += Suggestion(SuggestionType.IDEA_PARKING, "Mover ideias",
                "${ideias.size} ideias misturadas com tarefas de cliente. Posso mover para revisar depois?",
                null, urgency = 2, usefulness = 5, annoyanceRisk = 3, requiresConfirmation = true)
        }

        return result.take(5)
    }
}
