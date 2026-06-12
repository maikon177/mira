package com.pata3d.mira.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pata3d.mira.data.AcaoIA
import com.pata3d.mira.data.ChatMessage
import com.pata3d.mira.data.MiraRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ChatViewModel(private val repo: MiraRepository) : ViewModel() {

    val mensagens: StateFlow<List<ChatMessage>> =
        repo.observarChat().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _enviando = MutableStateFlow(false)
    val enviando: StateFlow<Boolean> = _enviando.asStateFlow()

    private val _erro = MutableStateFlow<String?>(null)
    val erro: StateFlow<String?> = _erro.asStateFlow()

    /** Ações propostas no último turno, aguardando confirmação. */
    private val _acoesPendentes = MutableStateFlow<List<AcaoIA>>(emptyList())
    val acoesPendentes: StateFlow<List<AcaoIA>> = _acoesPendentes.asStateFlow()

    fun temChave(): Boolean = repo.hasDeepSeekKey()

    fun enviar(texto: String) {
        if (texto.isBlank() || _enviando.value) return
        viewModelScope.launch {
            _enviando.value = true
            _erro.value = null
            _acoesPendentes.value = emptyList()
            runCatching { repo.enviarMensagemChat(texto.trim()) }
                .onSuccess { _acoesPendentes.value = it.acoes }
                .onFailure { _erro.value = "Nao consegui fechar essa resposta agora. Tenta de novo em seguida." }
            _enviando.value = false
        }
    }

    fun confirmarAcao(acao: AcaoIA) = viewModelScope.launch {
        repo.aplicarAcaoIA(acao)
        _acoesPendentes.value = _acoesPendentes.value - acao
    }

    fun ignorarAcao(acao: AcaoIA) {
        _acoesPendentes.value = _acoesPendentes.value - acao
    }

    fun limpar() = viewModelScope.launch {
        repo.limparChat()
        _acoesPendentes.value = emptyList()
    }
}
