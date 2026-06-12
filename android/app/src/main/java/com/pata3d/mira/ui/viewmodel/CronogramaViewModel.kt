package com.pata3d.mira.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pata3d.mira.data.BlocoDisponibilidade
import com.pata3d.mira.data.MiraRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CronogramaViewModel(private val repo: MiraRepository) : ViewModel() {
    val blocos = repo.observarBlocos()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun salvar(b: BlocoDisponibilidade) = viewModelScope.launch { repo.salvarBloco(b) }
    fun deletar(id: Long) = viewModelScope.launch { repo.deletarBloco(id) }
}
