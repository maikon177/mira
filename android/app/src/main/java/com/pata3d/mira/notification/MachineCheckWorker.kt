package com.pata3d.mira.notification

import android.content.Context
import androidx.work.*
import com.pata3d.mira.MiraApplication
import java.util.concurrent.TimeUnit

class MachineCheckWorker(ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {

    override suspend fun doWork(): Result {
        val app      = applicationContext as MiraApplication
        val repo     = app.repository
        val tarefaId = inputData.getString(KEY_TAREFA_ID) ?: return Result.failure()
        val fase     = inputData.getString(KEY_FASE) ?: "Impressão"

        val tarefa = repo.buscarTarefa(tarefaId) ?: return Result.success()
        if (tarefa.status != "aberta") return Result.success()

        MiraNotificationManager.mostrarCheckinMaquina(applicationContext, tarefa, fase)
        return Result.success()
    }

    companion object {
        const val KEY_TAREFA_ID = "tarefa_id"
        const val KEY_FASE      = "fase"

        fun agendarFase(ctx: Context, tarefaId: String, fase: String, delayMin: Long) {
            if (delayMin <= 0) return
            WorkManager.getInstance(ctx).enqueue(
                OneTimeWorkRequestBuilder<MachineCheckWorker>()
                    .setInitialDelay(delayMin, TimeUnit.MINUTES)
                    .setInputData(workDataOf(KEY_TAREFA_ID to tarefaId, KEY_FASE to fase))
                    .addTag("mira_machine_$tarefaId")
                    .build()
            )
        }

        fun cancelar(ctx: Context, tarefaId: String) {
            WorkManager.getInstance(ctx).cancelAllWorkByTag("mira_machine_$tarefaId")
        }
    }
}
