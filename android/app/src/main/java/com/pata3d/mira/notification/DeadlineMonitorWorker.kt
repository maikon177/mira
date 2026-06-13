package com.pata3d.mira.notification

import android.content.Context
import androidx.work.*
import com.pata3d.mira.MiraApplication
import com.pata3d.mira.data.NivelRisco
import com.pata3d.mira.data.TipoAlerta
import com.pata3d.mira.domain.CalculadorRisco
import java.util.concurrent.TimeUnit

class DeadlineMonitorWorker(ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {

    override suspend fun doWork(): Result {
        val app   = applicationContext as MiraApplication
        val repo  = app.repository
        val prefs = repo.prefsRef

        if (!prefs.notificacoesAtivas || !prefs.alertasRiscoPrazoAtivos) return Result.success()

        val agora   = System.currentTimeMillis()
        val tarefas = repo.listarAbertas()
            .filter { it.prazoEm != null && it.tipoAlerta == TipoAlerta.RISCO_PRAZO.name }

        for (tarefa in tarefas) {
            val prazo = tarefa.prazoEm ?: continue
            val tempoTotalMin = CalculadorRisco.tempoTotalProducaoMin(
                tarefa.tempoPrepMin,
                tarefa.tempoMaquinaMin,
                tarefa.tempoSecagemMin,
                tarefa.tempoFinalMin,
                tarefa.tempoEstimadoMin,
            )
            val nivel = CalculadorRisco.calcular(
                prazoEm           = prazo,
                tempoTotalMin     = tempoTotalMin,
                silencioInicioMin = prefs.silencioInicioMin,
                silencioFimMin    = prefs.silencioFimMin,
            )

            repo.atualizarNivelRisco(tarefa.id, nivel)

            val intervaloMin = CalculadorRisco.intervaloAlertaMin(nivel)
            val ultimaNotif  = tarefa.ultimaNotificacaoEm ?: 0L
            val minutosDesde = (agora - ultimaNotif) / 60_000L

            if (minutosDesde >= intervaloMin) {
                MiraNotificationManager.mostrarAlertaRisco(applicationContext, tarefa, nivel)
            }
        }

        return Result.success()
    }

    companion object {
        private const val TAG = "mira_deadline_monitor"

        fun agendar(ctx: Context) {
            val req = PeriodicWorkRequestBuilder<DeadlineMonitorWorker>(15, TimeUnit.MINUTES)
                .addTag(TAG)
                .setBackoffCriteria(BackoffPolicy.LINEAR, 5, TimeUnit.MINUTES)
                .build()
            WorkManager.getInstance(ctx).enqueueUniquePeriodicWork(
                TAG,
                ExistingPeriodicWorkPolicy.KEEP,
                req,
            )
        }
    }
}
