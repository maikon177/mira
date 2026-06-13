package com.pata3d.mira

import android.app.Application
import android.provider.Settings
import com.pata3d.mira.bubble.BolhaService
import com.pata3d.mira.data.DeepSeekClient
import com.pata3d.mira.data.MiraDatabase
import com.pata3d.mira.data.MiraPrefs
import com.pata3d.mira.data.MiraRepository
import com.pata3d.mira.notification.CheckInWorker
import com.pata3d.mira.notification.DeadlineMonitorWorker
import com.pata3d.mira.notification.MiraNotificationManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class MiraApplication : Application() {
    val prefs by lazy { MiraPrefs(this) }
    val deepSeekClient by lazy { DeepSeekClient(prefs) }
    val repository by lazy { MiraRepository(MiraDatabase.get(this), prefs, deepSeekClient) }
    val miraBrain: com.pata3d.mira.brain.MiraBrain by lazy {
        com.pata3d.mira.brain.MiraBrain(
            contextBuilder = com.pata3d.mira.brain.ContextSnapshotBuilder(repository),
            nextActionEngine = com.pata3d.mira.brain.NextActionEngine(com.pata3d.mira.brain.PriorityEngine()),
            suggestionEngine = com.pata3d.mira.brain.SuggestionEngine(),
            notificationPlanner = com.pata3d.mira.brain.NotificationPlanner(),
            autonomyGate = com.pata3d.mira.brain.AutonomyGate { com.pata3d.mira.brain.AutonomyMode.ASSISTANT },
            memoryEngine = com.pata3d.mira.brain.LearningMemoryEngine(),
            repo = repository,
            decisionLogDao = com.pata3d.mira.data.MiraDatabase.get(this).decisionLogDao(),
        )
    }

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        MiraNotificationManager.criarCanais(this)
        CheckInWorker.agendar(this)
        CheckInWorker.agendarCheckins(
            this,
            prefs.lembreteManhaMin,
            prefs.checkinTardeMin,
            prefs.checkinNoiteMin,
        )
        DeadlineMonitorWorker.agendar(this)
        if (prefs.bolhaAtiva && Settings.canDrawOverlays(this)) {
            BolhaService.iniciar(this)
        }
        appScope.launch {
            repository.semearCronogramaSeNecessario()
            CheckInWorker.dispararAgora(this@MiraApplication)
        }
    }
}
