package com.pata3d.mira.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pata3d.mira.data.MiraRepository
import com.pata3d.mira.data.NovaEtapa
import com.pata3d.mira.data.RevisaoResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class RevisaoState {
    object Idle : RevisaoState()
    object Carregando : RevisaoState()
    data class Sucesso(val result: RevisaoResult) : RevisaoState()
    data class Erro(val msg: String) : RevisaoState()
}

class EntradaViewModel(private val repo: MiraRepository) : ViewModel() {

    private val _revisao = MutableStateFlow<RevisaoState>(RevisaoState.Idle)
    val revisao: StateFlow<RevisaoState> = _revisao.asStateFlow()

    fun temChave(): Boolean = repo.hasDeepSeekKey()

    fun adicionarDireto(
        texto: String,
        tempoTotalMin: Int?,
        etapasTexto: String,
        onPronto: () -> Unit,
    ) = viewModelScope.launch {
        val etapas = parseEtapas(etapasTexto)
        if (etapas.isNotEmpty()) {
            repo.criarTarefa(
                titulo = texto.trim(),
                tempoEstimadoMin = tempoTotalMin ?: etapas.mapNotNull { it.tempoEstimadoMin }.sum().takeIf { it > 0 },
                proximaAcao = etapas.first().titulo,
                etapas = etapas,
            )
        } else {
            texto.split("\n").map { it.trim() }.filter { it.isNotEmpty() }
                .forEach { repo.criarTarefa(titulo = it, tempoEstimadoMin = tempoTotalMin) }
        }
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
