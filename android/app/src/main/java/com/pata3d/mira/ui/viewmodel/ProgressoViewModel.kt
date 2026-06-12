package com.pata3d.mira.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pata3d.mira.data.MiraRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar

data class ProgressoUiState(
    val concluidasHoje: Int = 0,
    val concluidasSemana: Int = 0,
    val abertas: Int = 0,
    val sequenciaDias: Int = 0,
    val porDiaSemana: List<Pair<String, Int>> = emptyList(),
)

class ProgressoViewModel(private val repo: MiraRepository) : ViewModel() {

    private val _ui = MutableStateFlow(ProgressoUiState())
    val ui: StateFlow<ProgressoUiState> = _ui.asStateFlow()

    init {
        viewModelScope.launch {
            repo.observarTarefas().collect { recalcular() }
        }
    }

    private suspend fun recalcular() {
        val inicioSemana = inicioDoDia(-6)
        val historico = repo.listarHistorico(inicioSemana)
        val concluidas = historico.filter { it.tipo == "tarefa_concluida" }

        val inicioHoje = inicioDoDia(0)
        val concluidasHoje = concluidas.count { it.em >= inicioHoje }

        val abertas = repo.listarAbertas().size

        // Concluídas por dia da semana (últimos 7 dias).
        val nomes = listOf("Seg", "Ter", "Qua", "Qui", "Sex", "Sáb", "Dom")
        val porDia = (0..6).map { offset ->
            val ini = inicioDoDia(-offset)
            val fim = ini + 86_400_000L
            val cal = Calendar.getInstance().apply { timeInMillis = ini }
            val nomeIdx = when (cal.get(Calendar.DAY_OF_WEEK)) {
                Calendar.MONDAY -> 0; Calendar.TUESDAY -> 1; Calendar.WEDNESDAY -> 2
                Calendar.THURSDAY -> 3; Calendar.FRIDAY -> 4; Calendar.SATURDAY -> 5; else -> 6
            }
            nomes[nomeIdx] to concluidas.count { it.em in ini until fim }
        }.reversed()

        // Sequência de dias com ao menos 1 conclusão.
        var seq = 0
        for (offset in 0..6) {
            val ini = inicioDoDia(-offset); val fim = ini + 86_400_000L
            if (concluidas.any { it.em in ini until fim }) seq++ else if (offset > 0) break
        }

        _ui.value = ProgressoUiState(
            concluidasHoje = concluidasHoje,
            concluidasSemana = concluidas.size,
            abertas = abertas,
            sequenciaDias = seq,
            porDiaSemana = porDia,
        )
    }

    private fun inicioDoDia(offsetDias: Int): Long = Calendar.getInstance().apply {
        add(Calendar.DAY_OF_YEAR, offsetDias)
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }.timeInMillis
}
