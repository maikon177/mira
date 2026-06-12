package com.pata3d.mira.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pata3d.mira.data.MiraRepository
import com.pata3d.mira.data.Tarefa
import com.pata3d.mira.domain.Sugestoes
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class CalendarioUiState(
    val diaSelecionado: Long = Sugestoes.inicioDoDia(System.currentTimeMillis()),
    val tarefasDoDia: List<Tarefa> = emptyList(),
    val semData: List<Tarefa> = emptyList(),
    /** Dias (início-do-dia em millis) que têm ao menos 1 tarefa, p/ marcar a faixa. */
    val diasComTarefa: Set<Long> = emptySet(),
    val todas: List<Tarefa> = emptyList(),
)

class CalendarioViewModel(private val repo: MiraRepository) : ViewModel() {

    private val _ui = MutableStateFlow(CalendarioUiState())
    val ui: StateFlow<CalendarioUiState> = _ui.asStateFlow()

    init {
        viewModelScope.launch {
            repo.observarTarefas().collect { recalcular(it) }
        }
    }

    private fun recalcular(todas: List<Tarefa>) {
        val abertas = todas.filter { it.status !in listOf("concluida", "cancelada") }
        val dia = _ui.value.diaSelecionado

        val doDia = abertas
            .filter { it.dataAgendada != null && Sugestoes.mesmoDia(it.dataAgendada, dia) }
            .sortedBy { it.dataAgendada }

        val semData = abertas.filter { it.dataAgendada == null }

        val diasComTarefa = abertas
            .mapNotNull { it.dataAgendada?.let { d -> Sugestoes.inicioDoDia(d) } }
            .toSet()

        _ui.value = _ui.value.copy(
            tarefasDoDia = doDia,
            semData = semData,
            diasComTarefa = diasComTarefa,
            todas = abertas,
        )
    }

    fun selecionarDia(inicioDoDia: Long) {
        _ui.value = _ui.value.copy(diaSelecionado = inicioDoDia)
        viewModelScope.launch { recalcular(repo.listarTarefas()) }
    }

    fun agendarPara(id: String, inicioDoDia: Long?) = viewModelScope.launch {
        repo.agendarTarefa(id, inicioDoDia)
    }

    fun concluir(id: String) = viewModelScope.launch { repo.concluirTarefa(id) }
}
