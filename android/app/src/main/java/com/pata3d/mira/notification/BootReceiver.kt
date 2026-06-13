package com.pata3d.mira.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.pata3d.mira.MiraApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(ctx: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        CheckInWorker.agendar(ctx)
        CheckInWorker.dispararAgora(ctx)
        val repo = (ctx.applicationContext as MiraApplication).repository
        // TODO Task 12: val prefs = repo.prefsRef
        // TODO Task 12: CheckInWorker.agendarCheckins(ctx, prefs.lembreteManhaMin, prefs.checkinTardeMin, prefs.checkinNoiteMin)
        CoroutineScope(Dispatchers.IO).launch {
            val compromissos = repo.listarCompromissosFuturos()
            AlarmScheduler.reagendarTodos(ctx, compromissos)
        }
    }
}
