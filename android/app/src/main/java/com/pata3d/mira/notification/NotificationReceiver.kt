package com.pata3d.mira.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.pata3d.mira.MiraApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NotificationReceiver : BroadcastReceiver() {

    override fun onReceive(ctx: Context, intent: Intent) {
        val repo = (ctx.applicationContext as MiraApplication).repository
        val id   = intent.getStringExtra(EXTRA_ID) ?: return
        val pending = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                when (intent.action) {
                    ACTION_CONCLUIR  -> { repo.concluirTarefa(id); CheckInWorker.dispararAgora(ctx) }
                    ACTION_ADIAR     -> { repo.adiarTarefa(id, 30); CheckInWorker.dispararAgora(ctx) }
                    ACTION_INICIAR   -> { repo.marcarTarefaIniciada(id); CheckInWorker.dispararAgora(ctx) }
                    ACTION_DIVIDIR   -> { /* abre app; PendingIntent já navega */ }
                    ACTION_SONECA_5  -> { repo.adiarTarefa(id, 5); CheckInWorker.dispararAgora(ctx) }
                    ACTION_SONECA_10 -> { repo.adiarTarefa(id, 10); CheckInWorker.dispararAgora(ctx) }
                    ACTION_SONECA_30 -> { repo.adiarTarefa(id, 30); CheckInWorker.dispararAgora(ctx) }
                    "com.pata3d.mira.ALARME_COMPROMISSO" -> {
                        val tarefa = repo.buscarTarefa(id) ?: return@launch
                        MiraNotificationManager.mostrarAlarmeCompromisso(ctx, tarefa)
                    }
                }
            } finally {
                pending.finish()
            }
        }
    }

    companion object {
        const val ACTION_CONCLUIR  = "com.pata3d.mira.CONCLUIR_TAREFA"
        const val ACTION_ADIAR     = "com.pata3d.mira.ADIAR_TAREFA"
        const val ACTION_INICIAR   = "com.pata3d.mira.INICIAR_TAREFA"
        const val ACTION_DIVIDIR   = "com.pata3d.mira.DIVIDIR_TAREFA"
        const val ACTION_SONECA_5  = "com.pata3d.mira.SONECA_5"
        const val ACTION_SONECA_10 = "com.pata3d.mira.SONECA_10"
        const val ACTION_SONECA_30 = "com.pata3d.mira.SONECA_30"
        const val EXTRA_ID         = "tarefa_id"
    }
}
