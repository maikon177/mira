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
                    ACTION_CONCLUIR -> repo.concluirTarefa(id)
                    ACTION_ADIAR    -> repo.adiarTarefa(id, 30)
                }
                CheckInWorker.dispararAgora(ctx)
            } finally {
                pending.finish()
            }
        }
    }

    companion object {
        const val ACTION_CONCLUIR = "com.pata3d.mira.CONCLUIR_TAREFA"
        const val ACTION_ADIAR    = "com.pata3d.mira.ADIAR_TAREFA"
        const val EXTRA_ID        = "tarefa_id"
    }
}
