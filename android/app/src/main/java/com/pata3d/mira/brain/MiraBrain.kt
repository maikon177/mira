package com.pata3d.mira.brain

import com.pata3d.mira.brain.models.AiIntent
import com.pata3d.mira.brain.models.MemoryFact
import com.pata3d.mira.brain.models.NextAction
import com.pata3d.mira.brain.models.PermissionResult
import com.pata3d.mira.brain.models.Suggestion
import com.pata3d.mira.data.DecisionLogDao
import com.pata3d.mira.data.DecisionLogDb
import com.pata3d.mira.data.MiraRepository
import com.pata3d.mira.data.Tarefa

class MiraBrain(
    val contextBuilder: ContextSnapshotBuilder,
    private val nextActionEngine: NextActionEngine,
    private val suggestionEngine: SuggestionEngine,
    private val notificationPlanner: NotificationPlanner,
    private val autonomyGate: AutonomyGate,
    private val memoryEngine: LearningMemoryEngine,
    private val repo: MiraRepository,
    private val decisionLogDao: DecisionLogDao,
) {

    /** Ranking de ações para a Tela Hoje (hero + trocar). Sem efeito colateral. */
    suspend fun rank(tarefas: List<Tarefa>): List<NextAction> {
        val context = contextBuilder.build()
        return nextActionEngine.rank(tarefas, context)
    }

    /** Próxima ação principal (topo do rank). */
    suspend fun decideNow(): NextAction? = rank(repo.listarAbertas()).firstOrNull()

    /** Registra a decisão — chamar só quando o hero muda (evita inflar a tabela). */
    suspend fun logDecision(action: NextAction) {
        decisionLogDao.inserir(DecisionLogDb(selectedTaskId = action.taskId, reason = action.reason))
    }

    /** Purga logs de decisão antigos para a tabela não crescer sem limite (padrão: 14 dias). */
    suspend fun limparLogsAntigos(antesDe: Long = System.currentTimeMillis() - 14L * 24 * 3_600_000L) {
        decisionLogDao.limparAntigos(antesDe)
    }

    /** Revisão horária: melhor sugestão se vale notificar; null caso contrário. */
    suspend fun reviewHourly(): Suggestion? {
        val context = contextBuilder.build()
        val suggestions = suggestionEngine.generate(repo.listarAbertas(), context)
        return notificationPlanner.pickBest(suggestions, context)
    }

    fun evaluateAiAction(intent: AiIntent): PermissionResult = autonomyGate.canExecute(intent)

    /** Mapeia o `tipo` textual de AcaoIA para um AiIntent. */
    fun intentDe(tipo: String): AiIntent = when (tipo) {
        "criar_tarefa"    -> AiIntent.CREATE_TASK
        "agendar_tarefa"  -> AiIntent.SCHEDULE_TASK
        "concluir_tarefa" -> AiIntent.COMPLETE_TASK
        "editar_tarefa"   -> AiIntent.UPDATE_TASK
        else              -> AiIntent.UNKNOWN
    }

    suspend fun learnPatterns(): List<MemoryFact> {
        val tarefas = repo.listarTarefas()
        val historico = repo.listarHistorico(System.currentTimeMillis() - 30L * 24 * 3_600_000L)
        return memoryEngine.derivePatterns(tarefas, historico)
    }
}
