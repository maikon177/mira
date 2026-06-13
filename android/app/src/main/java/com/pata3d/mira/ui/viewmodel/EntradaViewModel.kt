package com.pata3d.mira.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pata3d.mira.data.MiraRepository
import com.pata3d.mira.data.NovaEtapa
import com.pata3d.mira.data.RevisaoResult
import com.pata3d.mira.data.TipoAlerta
import com.pata3d.mira.data.TipoTarefa
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed class RevisaoState {
    object Idle : RevisaoState()
    object Carregando : RevisaoState()
    data class Sucesso(val result: RevisaoResult) : RevisaoState()
    data class Erro(val msg: String) : RevisaoState()
}

data class EntradaUiState(
    val tipoTarefa: String = TipoTarefa.SIMPLES.name,
    val prazoEm: Long? = null,
    val compromissoEm: Long? = null,
    val lembreteEm: Long? = null,
    val microPasso: String = "",
    val protegerPrazo: Boolean = false,
    val tempoPrepMin: String = "",
    val tempoMaquinaMin: String = "",
    val tempoSecagemMin: String = "",
    val tempoFinalMin: String = "",
)

class EntradaViewModel(private val repo: MiraRepository) : ViewModel() {

    private val _revisao = MutableStateFlow<RevisaoState>(RevisaoState.Idle)
    val revisao: StateFlow<RevisaoState> = _revisao.asStateFlow()

    private val _ui = MutableStateFlow(EntradaUiState())
    val ui: StateFlow<EntradaUiState> = _ui.asStateFlow()

    fun temChave(): Boolean = repo.hasDeepSeekKey()

    fun setTipoTarefa(v: String) { _ui.update { it.copy(tipoTarefa = v) } }
    fun setPrazoEm(v: Long?) { _ui.update { it.copy(prazoEm = v) } }
    fun setCompromissoEm(v: Long?) { _ui.update { it.copy(compromissoEm = v) } }
    fun setLembreteEm(v: Long?) { _ui.update { it.copy(lembreteEm = v) } }
    fun setMicroPasso(v: String) { _ui.update { it.copy(microPasso = v) } }
    fun toggleProtegerPrazo() { _ui.update { it.copy(protegerPrazo = !it.protegerPrazo) } }
    fun setTempoPrepMin(v: String) { _ui.update { it.copy(tempoPrepMin = v) } }
    fun setTempoMaquinaMin(v: String) { _ui.update { it.copy(tempoMaquinaMin = v) } }
    fun setTempoSecagemMin(v: String) { _ui.update { it.copy(tempoSecagemMin = v) } }
    fun setTempoFinalMin(v: String) { _ui.update { it.copy(tempoFinalMin = v) } }

    fun adicionarDireto(
        texto: String,
        tempoTotalMin: Int?,
        etapasTexto: String,
        onPronto: () -> Unit,
    ) = viewModelScope.launch {
        val uiState = _ui.value
        val etapas = parseEtapas(etapasTexto)
        val tipoAlerta = when (uiState.tipoTarefa) {
            TipoTarefa.LEMBRETE.name    -> TipoAlerta.LEMBRETE.name
            TipoTarefa.COMPROMISSO.name -> TipoAlerta.ALARME_EXATO.name
            TipoTarefa.ENTREGA.name     -> TipoAlerta.RISCO_PRAZO.name
            TipoTarefa.PRODUCAO.name    -> TipoAlerta.RISCO_PRAZO.name
            else                        -> TipoAlerta.NENHUM.name
        }
        if (etapas.isNotEmpty()) {
            repo.criarTarefa(
                titulo = texto.trim(),
                tempoEstimadoMin = tempoTotalMin ?: etapas.mapNotNull { it.tempoEstimadoMin }.sum().takeIf { it > 0 },
                proximaAcao = etapas.first().titulo,
                etapas = etapas,
                tipoTarefa = uiState.tipoTarefa,
                prazoEm = uiState.prazoEm,
                compromissoEm = uiState.compromissoEm,
                lembreteEm = uiState.lembreteEm,
                microPasso = uiState.microPasso,
                protegerPrazo = uiState.protegerPrazo,
                isTarefaMaquina = uiState.tipoTarefa == TipoTarefa.PRODUCAO.name,
                tempoPrepMin = uiState.tempoPrepMin.toIntOrNull(),
                tempoMaquinaMin = uiState.tempoMaquinaMin.toIntOrNull(),
                tempoSecagemMin = uiState.tempoSecagemMin.toIntOrNull(),
                tempoFinalMin = uiState.tempoFinalMin.toIntOrNull(),
                tipoAlerta = tipoAlerta,
            )
        } else {
            texto.split("\n").map { it.trim() }.filter { it.isNotEmpty() }
                .forEachIndexed { index, linha ->
                    repo.criarTarefa(
                        titulo = linha,
                        tempoEstimadoMin = tempoTotalMin,
                        tipoTarefa = if (index == 0) uiState.tipoTarefa else TipoTarefa.SIMPLES.name,
                        prazoEm = if (index == 0) uiState.prazoEm else null,
                        compromissoEm = if (index == 0) uiState.compromissoEm else null,
                        lembreteEm = if (index == 0) uiState.lembreteEm else null,
                        microPasso = if (index == 0) uiState.microPasso else "",
                        protegerPrazo = if (index == 0) uiState.protegerPrazo else false,
                        isTarefaMaquina = if (index == 0) uiState.tipoTarefa == TipoTarefa.PRODUCAO.name else false,
                        tempoPrepMin = if (index == 0) uiState.tempoPrepMin.toIntOrNull() else null,
                        tempoMaquinaMin = if (index == 0) uiState.tempoMaquinaMin.toIntOrNull() else null,
                        tempoSecagemMin = if (index == 0) uiState.tempoSecagemMin.toIntOrNull() else null,
                        tempoFinalMin = if (index == 0) uiState.tempoFinalMin.toIntOrNull() else null,
                        tipoAlerta = if (index == 0) tipoAlerta else TipoAlerta.NENHUM.name,
                    )
                }
        }
        _ui.update { EntradaUiState() }
        onPronto()
    }

    fun revisarComIA(texto: String) = viewModelScope.launch {
        _revisao.value = RevisaoState.Carregando
        _revisao.value = runCatching { RevisaoState.Sucesso(repo.revisarEntrada(texto)) }
            .getOrElse { RevisaoState.Erro(it.message ?: "Erro desconhecido") }
    }

    fun salvarRevisao(result: RevisaoResult, onPronto: () -> Unit) = viewModelScope.launch {
        repo.salvarTarefasRevisadas(result.tarefas)
        _revisao.value = RevisaoState.Idle
        onPronto()
    }

    fun resetar() {
        _revisao.value = RevisaoState.Idle
    }

    private fun parseEtapas(texto: String): List<NovaEtapa> =
        texto.lineSequence()
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .map { linha ->
                val partes = linha.split("|").map { it.trim() }
                val titulo = partes.firstOrNull().orEmpty()
                val tempo = partes.getOrNull(1)?.toIntOrNull()
                NovaEtapa(titulo = titulo, tempoEstimadoMin = tempo)
            }
            .filter { it.titulo.isNotBlank() }
            .toList()
}
