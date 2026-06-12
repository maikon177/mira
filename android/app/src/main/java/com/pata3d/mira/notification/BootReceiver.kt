package com.pata3d.mira.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(ctx: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            CheckInWorker.agendar(ctx)
            CheckInWorker.dispararAgora(ctx)
        }
    }
}
