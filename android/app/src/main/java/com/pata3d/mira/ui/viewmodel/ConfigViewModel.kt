package com.pata3d.mira.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pata3d.mira.data.MiraPrefs
import com.pata3d.mira.data.MiraRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ConfigUiState(
    val chave: String = "",
    val chaveSalva: Boolean = false,
    val modoAvancado: Boolean = false,
    val notificacoesAtivas: Boolean = true,
    val lembreteManhaMin: Int = 450,
    val silencioInicioMin: Int = 22 * 60,
    val silencioFimMin: Int = 7 * 60,
    val tema: String = "escuro",
    val maxSugestoes: Int = 3,
    val sugestaoDestravarAtiva: Boolean = true,
)

class ConfigViewModel(private val repo: MiraRepository) : ViewModel() {

    private val prefs: MiraPrefs = repo.prefsRef

    private val _ui = MutableStateFlow(carregar())
    val ui: StateFlow<ConfigUiState> = _ui.asStateFlow()

    private fun carregar() = ConfigUiState(
        chave = repo.getDeepSeekKey(),
        modoAvancado = prefs.modoAvancado,
        notificacoesAtivas = prefs.notificacoesAtivas,
        lembreteManhaMin = prefs.lembreteManhaMin,
        silencioInicioMin = prefs.silencioInicioMin,
        silencioFimMin = prefs.silencioFimMin,
        tema = prefs.tema,
        maxSugestoes = prefs.maxSugestoes,
        sugestaoDestravarAtiva = prefs.sugestaoDestravarAtiva,
    )

    fun setChave(v: String) { _ui.value = _ui.value.copy(chave = v, chaveSalva = false) }
    fun salvarChave() {
        repo.setDeepSeekKey(_ui.value.chave.trim())
        _ui.value = _ui.value.copy(chaveSalva = true)
    }

    fun toggleModoAvancado(v: Boolean) { prefs.modoAvancado = v; _ui.value = _ui.value.copy(modoAvancado = v) }
    fun toggleNotificacoes(v: Boolean) { prefs.notificacoesAtivas = v; _ui.value = _ui.value.copy(notificacoesAtivas = v) }
    fun setLembreteManha(min: Int) { prefs.lembreteManhaMin = min; _ui.value = _ui.value.copy(lembreteManhaMin = min) }
    fun setSilencio(inicio: Int, fim: Int) {
        prefs.silencioInicioMin = inicio; prefs.silencioFimMin = fim
        _ui.value = _ui.value.copy(silencioInicioMin = inicio, silencioFimMin = fim)
    }
    fun setTema(v: String) { prefs.tema = v; _ui.value = _ui.value.copy(tema = v) }
    fun setMaxSugestoes(v: Int) { prefs.maxSugestoes = v; _ui.value = _ui.value.copy(maxSugestoes = v) }
    fun toggleDestravar(v: Boolean) { prefs.sugestaoDestravarAtiva = v; _ui.value = _ui.value.copy(sugestaoDestravarAtiva = v) }

    fun limparChat() = viewModelScope.launch { repo.limparChat() }
}
