package com.pata3d.mira.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.pata3d.mira.data.MiraRepository

class MiraViewModelFactory(private val repo: MiraRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = when {
        modelClass.isAssignableFrom(HojeViewModel::class.java)       -> HojeViewModel(repo)
        modelClass.isAssignableFrom(CalendarioViewModel::class.java) -> CalendarioViewModel(repo)
        modelClass.isAssignableFrom(ChatViewModel::class.java)       -> ChatViewModel(repo)
        modelClass.isAssignableFrom(ProgressoViewModel::class.java)  -> ProgressoViewModel(repo)
        modelClass.isAssignableFrom(EntradaViewModel::class.java)    -> EntradaViewModel(repo)
        modelClass.isAssignableFrom(ConfigViewModel::class.java)      -> ConfigViewModel(repo)
        modelClass.isAssignableFrom(CronogramaViewModel::class.java) -> CronogramaViewModel(repo)
        else -> throw IllegalArgumentException("ViewModel desconhecido: ${modelClass.name}")
    } as T
}
