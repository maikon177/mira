package com.pata3d.mira.brain

import com.pata3d.mira.brain.models.MemoryCategory
import com.pata3d.mira.brain.models.MemoryFact
import com.pata3d.mira.data.Historico
import com.pata3d.mira.data.Tarefa

class LearningMemoryEngine {

    fun derivePatterns(tarefas: List<Tarefa>, historico: List<Historico>): List<MemoryFact> {
        val facts = mutableListOf<MemoryFact>()
        val concluidas = tarefas.filter { it.status == "concluida" }

        val rapidas = concluidas.filter { (it.tempoEstimadoMin ?: 999) <= 30 }
        if (concluidas.isNotEmpty() && rapidas.size.toDouble() / concluidas.size > 0.6) {
            facts += MemoryFact("Conclui melhor tarefas de até 30 minutos.", MemoryCategory.BEHAVIOR, 0.8f)
        }

        val adiadas = tarefas.filter { it.adiamentos >= 3 && it.microPasso.isBlank() }
        if (adiadas.size >= 2) {
            facts += MemoryFact("Tarefas vagas sem micro-passo são frequentemente adiadas.", MemoryCategory.PATTERN, 0.9f)
        }

        val cliente = concluidas.filter {
            Regex("cliente|orcament|venda", RegexOption.IGNORE_CASE).containsMatchIn("${it.titulo} ${it.categoria}")
        }
        if (cliente.size >= 3) {
            facts += MemoryFact("Tarefas de cliente têm alta taxa de conclusão.", MemoryCategory.PREFERENCE, 0.85f)
        }

        return facts
    }
}
