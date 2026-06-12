package com.pata3d.mira.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.pata3d.mira.MainActivity
import com.pata3d.mira.R
import com.pata3d.mira.data.Tarefa

object MiraNotificationManager {

    const val CANAL_PERSISTENTE = "mira_persistente"
    const val CANAL_ALERTAS     = "mira_alertas"
    const val NOTIF_PERSISTENTE = 1
    const val NOTIF_CHECKIN     = 2

    fun criarCanais(ctx: Context) {
        val nm = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.createNotificationChannel(
            NotificationChannel(
                CANAL_PERSISTENTE,
                "Próxima tarefa",
                NotificationManager.IMPORTANCE_LOW,
            ).apply {
                description = "Mostra a tarefa mais importante no momento"
                setShowBadge(false)
            }
        )
        nm.createNotificationChannel(
            NotificationChannel(
                CANAL_ALERTAS,
                "Check-ins",
                NotificationManager.IMPORTANCE_DEFAULT,
            ).apply {
                description = "Lembretes nos horários de trabalho"
            }
        )
    }

    fun mostrarPersistente(ctx: Context, tarefa: Tarefa?) {
        val nm = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (tarefa == null) { nm.cancel(NOTIF_PERSISTENTE); return }

        val abrirApp = pendingActivity(ctx, 0)

        val base = tarefa.id.hashCode() and 0x7FFFFFFF
        val concluir = PendingIntent.getBroadcast(
            ctx, base,
            Intent(ctx, NotificationReceiver::class.java).apply {
                action = NotificationReceiver.ACTION_CONCLUIR
                putExtra(NotificationReceiver.EXTRA_ID, tarefa.id)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val adiar = PendingIntent.getBroadcast(
            ctx, base + 1,
            Intent(ctx, NotificationReceiver::class.java).apply {
                action = NotificationReceiver.ACTION_ADIAR
                putExtra(NotificationReceiver.EXTRA_ID, tarefa.id)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val subtitulo = tarefa.proximaAcao.ifBlank {
            tarefa.categoria + (tarefa.tempoEstimadoMin?.let { " · ~${it}min" } ?: "")
        }

        nm.notify(
            NOTIF_PERSISTENTE,
            NotificationCompat.Builder(ctx, CANAL_PERSISTENTE)
                .setSmallIcon(R.drawable.ic_notif)
                .setContentTitle(tarefa.titulo)
                .setContentText(subtitulo)
                .setContentIntent(abrirApp)
                .setOngoing(true)
                .setShowWhen(false)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .addAction(0, "✓ Concluir", concluir)
                .addAction(0, "⏱ +30min", adiar)
                .build()
        )
    }

    fun mostrarAlertaCheckin(ctx: Context, tarefa: Tarefa) {
        val nm = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val tempo = tarefa.tempoEstimadoMin?.let { " (~${it}min)" } ?: ""
        val detalhe = tarefa.proximaAcao.ifBlank { tarefa.motivo }

        nm.notify(
            NOTIF_CHECKIN,
            NotificationCompat.Builder(ctx, CANAL_ALERTAS)
                .setSmallIcon(R.drawable.ic_notif)
                .setContentTitle("Hora de focar")
                .setContentText("${tarefa.titulo}$tempo")
                .setStyle(
                    NotificationCompat.BigTextStyle()
                        .bigText("${tarefa.titulo}$tempo\n$detalhe".trim())
                )
                .setContentIntent(pendingActivity(ctx, 10))
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .build()
        )
    }

    private fun pendingActivity(ctx: Context, requestCode: Int): PendingIntent =
        PendingIntent.getActivity(
            ctx, requestCode,
            Intent(ctx, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
}
