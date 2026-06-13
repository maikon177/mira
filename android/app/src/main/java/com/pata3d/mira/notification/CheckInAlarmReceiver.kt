package com.pata3d.mira.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class CheckInAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(ctx: Context, intent: Intent) {
        CheckInWorker.dispararAgora(ctx, CheckInWorker.TIPO_CHECKIN)
    }
}
