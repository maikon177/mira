package com.pata3d.mira.notification

import android.content.Context
import androidx.work.*
import com.pata3d.mira.MiraApplication
import java.util.concurrent.TimeUnit

class MachineCheckWorker(ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {

    override suspend fun doWork(): Result {
        val app  = applicationContext as MiraApplication
        val repo = app.repository
        val prefs = repo.prefsRef

        if (!prefs.alertasMaquinaAtivos) return Result.success()

        val tarefaId = inputData.getString(KEY_TAREFA_ID) ?: return Result.success()
        val tarefa   = repo.buscarTarefa(tarefaId) ?: return Result.success()
        if (tarefa.status != "aberta") return Result.success()
        if (!tarefa.isTarefaMaquina) return Result.success()

        val agora      = System.currentTimeMillis()
        val iniciadaEm = tarefa.iniciadaEm ?: return Result.success()
        val decorrido  = TimeUnit.MILLISECONDS.toMinutes(agora - iniciadaEm)
        val prep       = tarefa.tempoPrepMin ?: 0
        val maquina    = tarefa.tempoMaquinaMin ?: 0
        val secagem    = tarefa.tempoSecagemMin ?: 0

        val faseAtual = when {
            decorrido < prep                       -> "Preparacao"
            decorrido < prep + maquina             -> "Impressao"
            decorrido < prep + maquina + secagem   -> "Secagem"
            else                                   -> "Finalizacao"
        }

        MiraNotificationManager.mostrarCheckinMaquina(applicationContext, tarefa, faseAtual)

        // Agendar próximo checkin para a fase seguinte
        val proximaFase: String?
        val delayParaProxima: Long
        when (faseAtual) {
            "Preparacao" -> {
                proximaFase     = "Impressao"
                delayParaProxima = (prep - decorrido).coerceAtLeast(1)
            }
            "Impressao" -> {
                proximaFase     = "Secagem"
                delayParaProxima = (prep + maquina - decorrido).coerceAtLeast(1)
            }
            "Secagem" -> {
                proximaFase     = "Finalizacao"
                delayParaProxima = (prep + maquina + secagem - decorrido).coerceAtLeast(1)
            }
            else -> {
                proximaFase     = null
                delayParaProxima = 0
            }
        }

        if (proximaFase != null) {
            agendarFase(applicationContext, tarefaId, proximaFase, delayParaProxima)
        }

        return Result.success()
    }

    companion object {
        const val KEY_TAREFA_ID = "tarefa_id"

        fun agendarFase(ctx: Context, tarefaId: String, fase: String, delayMin: Long) {
            if (delayMin <= 0) return
            WorkManager.getInstance(ctx).enqueue(
                OneTimeWorkRequestBuilder<MachineCheckWorker>()
                    .setInitialDelay(delayMin, TimeUnit.MINUTES)
                    .setInputData(workDataOf(KEY_TAREFA_ID to tarefaId))
                    .addTag("mira_machine_$tarefaId")
                    .build()
            )
        }

        fun cancelar(ctx: Context, tarefaId: String) {
            WorkManager.getInstance(ctx).cancelAllWorkByTag("mira_machine_$tarefaId")
        }
    }
}
