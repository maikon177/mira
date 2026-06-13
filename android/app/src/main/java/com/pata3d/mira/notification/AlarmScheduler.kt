package com.pata3d.mira.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.pata3d.mira.MainActivity
import com.pata3d.mira.data.Tarefa

object AlarmScheduler {

    private const val ACTION_ALARME_COMPROMISSO = "com.pata3d.mira.ALARME_COMPROMISSO"

    fun agendarCompromisso(ctx: Context, tarefa: Tarefa) {
        val compromissoEm = tarefa.compromissoEm ?: return
        val am = ctx.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val showIntent = PendingIntent.getActivity(
            ctx, 0,
            Intent(ctx, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE,
        )

        am.setAlarmClock(
            AlarmManager.AlarmClockInfo(compromissoEm, showIntent),
            pendingIntentParaTarefa(ctx, tarefa.id),
        )
    }

    fun cancelarCompromisso(ctx: Context, tarefaId: String) {
        val am = ctx.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        am.cancel(pendingIntentParaTarefa(ctx, tarefaId))
    }

    fun reagendarTodos(ctx: Context, tarefas: List<Tarefa>) {
        tarefas.forEach { agendarCompromisso(ctx, it) }
    }

    private fun pendingIntentParaTarefa(ctx: Context, tarefaId: String): PendingIntent {
        val intent = Intent(ctx, NotificationReceiver::class.java).apply {
            action = ACTION_ALARME_COMPROMISSO
            putExtra(NotificationReceiver.EXTRA_ID, tarefaId)
        }
        return PendingIntent.getBroadcast(
            ctx, tarefaId.hashCode() and 0x7FFFFFFF, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }
}
