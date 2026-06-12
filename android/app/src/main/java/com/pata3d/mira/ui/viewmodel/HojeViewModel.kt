package com.pata3d.mira.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pata3d.mira.data.EtapaTarefa
import com.pata3d.mira.data.MiraRepository
import com.pata3d.mira.data.ResumoEtapas
import com.pata3d.mira.data.Tarefa
import com.pata3d.mira.domain.ContextoAtual
import com.pata3d.mira.domain.Sugestao
import com.pata3d.mira.domain.Sugestoes
import com.pata3d.mira.domain.ordenarPorPrioridade
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

data class HojeUiState(
    val contexto: ContextoAtual? = null,
    val hero: Tarefa? = null,
    val heroEtapas: ResumoEtapas? = null,
    val sugestoes: List<Sugestao> = emptyList(),
    val restoDoDia: List<Tarefa> = emptyList(),
    val totalAbertas: Int = 0,
    val agoraMillis: Long = System.currentTimeMillis(),
)

class HojeViewModel(private val repo: MiraRepository) : ViewModel() {

    private val _ui = MutableStateFlow(HojeUiState())
    val ui: StateFlow<HojeUiState> = _ui.asStateFlow()

    private val dispensadas = mutableSetOf<String>()
    private var heroIndex = 0

    init {
        viewModelScope.launch {
            combine(repo.observarTarefas(), repo.observarEtapas()) { tarefas, etapas ->
                tarefas to etapas
            }.collect { (tarefas, etapas) ->
                recalcular(tarefas, etapas)
            }
        }

        viewModelScope.launch {
            while (true) {
                delay(1000)
                _ui.value = _ui.value.copy(agoraMillis = System.currentTimeMillis())
            }
        }
    }

    private suspend fun recalcular(todas: List<Tarefa>, todasEtapas: List<EtapaTarefa>) {
        val contexto = repo.contextoAgora()
        val memorias = repo.listarMemorias(apenasAtivas = true)
        val agora = System.currentTimeMillis()

        val disponiveis = todas.filter { t ->
            if (t.status in listOf("concluida", "cancelada")) return@filter false
            if (t.status == "adiada" && t.adiarAte != null && t.adiarAte > agora) return@filter false
            true
        }

        val ranking = ordenarPorPrioridade(disponiveis, memorias)
        if (heroIndex >= ranking.size) heroIndex = 0
        val hero = ranking.getOrNull(heroIndex)
        val heroEtapas = hero?.let { tarefa ->
            val etapasDaTarefa = todasEtapas.filter { it.tarefaId == tarefa.id }.sortedBy { it.ordem }
            if (etapasDaTarefa.isEmpty()) null
            else ResumoEtapas(
                etapas = etapasDaTarefa,
                etapaAtual = etapasDaTarefa.firstOrNull { it.status != "concluida" },
                concluidas = etapasDaTarefa.count { it.status == "concluida" },
                total = etapasDaTarefa.size,
            )
        }

        val sugestoes = Sugestoes.gerar(
            tarefas = disponiveis.filter { it.id != hero?.id },
            contexto = contexto,
            memorias = memorias,
            max = repo.prefsRef.maxSugestoes,
            destravarAtivo = repo.prefsRef.sugestaoDestravarAtiva,
            dispensadas = dispensadas,
        )

        val resto = ranking.filter { it.id != hero?.id }.take(5)

        _ui.value = HojeUiState(
            contexto = contexto,
            hero = hero,
            heroEtapas = heroEtapas,
            sugestoes = sugestoes,
            restoDoDia = resto,
            totalAbertas = disponiveis.size,
            agoraMillis = _ui.value.agoraMillis,
        )
    }

    fun concluir(id: String) = viewModelScope.launch { heroIndex = 0; repo.concluirTarefa(id) }
    fun iniciar(id: String) = viewModelScope.launch { repo.iniciarTarefa(id) }
    fun adiar(id: String, min: Int) = viewModelScope.launch { heroIndex = 0; repo.adiarTarefa(id, min) }
    fun cancelar(id: String) = viewModelScope.launch { heroIndex = 0; repo.cancelarTarefa(id) }
    fun dividir(id: String) = viewModelScope.launch { repo.adiarTarefa(id, 0) }

    fun iniciarCronometroTarefa(id: String) = viewModelScope.launch { repo.iniciarCronometroTarefa(id) }
    fun pausarCronometroTarefa(id: String) = viewModelScope.launch { repo.pausarCronometroTarefa(id) }
    fun iniciarCronometroEtapa(id: String) = viewModelScope.launch { repo.iniciarCronometroEtapa(id) }
    fun pausarCronometroEtapa(id: String) = viewModelScope.launch { repo.pausarCronometroEtapa(id) }
    fun concluirEtapa(id: String) = viewModelScope.launch { repo.concluirEtapa(id) }

    fun trocarHero() = viewModelScope.launch {
        heroIndex += 1
        recalcular(repo.listarTarefas(), repo.observarEtapasSnapshot())
    }

    fun agendar(id: String, quando: String) = viewModelScope.launch {
        val data = when (quando) {
            "hoje" -> Sugestoes.inicioDoDia(System.currentTimeMillis())
            "amanha" -> Sugestoes.inicioDoDia(System.currentTimeMillis() + 86_400_000L)
            else -> null
        }
        repo.agendarTarefa(id, data)
    }

    fun dispensarSugestao(id: String) = viewModelScope.launch {
        dispensadas += id
        recalcular(repo.listarTarefas(), repo.observarEtapasSnapshot())
    }
}
