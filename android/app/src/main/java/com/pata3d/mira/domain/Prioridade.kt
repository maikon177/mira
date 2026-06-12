package com.pata3d.mira.domain

import com.pata3d.mira.data.Memoria
import com.pata3d.mira.data.Tarefa

private val PESO_PRIORIDADE = mapOf(
    "Alta" to 100,
    "Média" to 50,
    "Media" to 50,
    "Baixa" to 20,
)

private data class CategoriaBonus(val regex: Regex, val bonus: Int)

private val CATEGORIA_BONUS = listOf(
    CategoriaBonus(Regex("cliente|orcament|venda|financ|nota fiscal", RegexOption.IGNORE_CASE), 40),
    CategoriaBonus(Regex("manuten|bico|impressora|produc", RegexOption.IGNORE_CASE), 30),
    CategoriaBonus(Regex("suprim|filament|estoque", RegexOption.IGNORE_CASE), 20),
)

fun scoreTarefa(tarefa: Tarefa, memorias: List<Memoria> = emptyList()): Int {
    var score = PESO_PRIORIDADE[tarefa.prioridade] ?: 50
    val texto = "${tarefa.categoria} ${tarefa.titulo}"
    for ((regex, bonus) in CATEGORIA_BONUS) {
        if (regex.containsMatchIn(texto)) {
            score += bonus
            break
        }
    }
    if (tarefa.tempoEstimadoMin != null && tarefa.tempoEstimadoMin <= 15) score += 15
    score += tarefa.adiamentos * 12
    score += bonusMemoria(tarefa, memorias)
    return score
}

fun ordenarPorPrioridade(tarefas: List<Tarefa>, memorias: List<Memoria> = emptyList()): List<Tarefa> =
    tarefas.sortedByDescending { scoreTarefa(it, memorias) }

fun proximaAcao(tarefas: List<Tarefa>, memorias: List<Memoria> = emptyList()): Tarefa? {
    val agora = System.currentTimeMillis()
    val disponiveis = tarefas.filter { t ->
        if (t.status in listOf("concluida", "cancelada")) return@filter false
        if (t.status == "adiada" && t.adiarAte != null && t.adiarAte > agora) return@filter false
        true
    }
    return ordenarPorPrioridade(disponiveis, memorias).firstOrNull()
}

private fun bonusMemoria(tarefa: Tarefa, memorias: List<Memoria>): Int {
    val textoTarefa = "${tarefa.categoria} ${tarefa.titulo}".lowercase()
    val business = memorias.filter { it.isActive && it.memoryType == "business" }
    var bonus = 0
    val reCliente = Regex("cliente|orcament|venda|financeiro")
    val reProd = Regex("producao|impressora|manuten|prazo")
    for (m in business) {
        val tm = m.content.lowercase()
        if (reCliente.containsMatchIn(tm) && reCliente.containsMatchIn(textoTarefa)) bonus += 10
        if (reProd.containsMatchIn(tm) && reProd.containsMatchIn(textoTarefa)) bonus += 10
    }
    return bonus.coerceAtMost(20)
}
