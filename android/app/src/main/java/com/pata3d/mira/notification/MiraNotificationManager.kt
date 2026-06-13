package com.pata3d.mira.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.pata3d.mira.MainActivity
import com.pata3d.mira.R
import com.pata3d.mira.data.NivelRisco
import com.pata3d.mira.data.Tarefa

object MiraNotificationManager {

    const val CANAL_PERSISTENTE  = "mira_persistente"
    const val CANAL_CHECKIN      = "mira_checkin"
    const val CANAL_ALARME       = "mira_alarme"
    const val CANAL_RISCO        = "mira_risco_prazo"
    const val CANAL_MAQUINA      = "mira_maquina"

    const val NOTIF_PERSISTENTE  = 1
    const val NOTIF_CHECKIN      = 2
    const val NOTIF_ALARME       = 3
    const val NOTIF_RISCO        = 4
    const val NOTIF_MAQUINA      = 5

    fun criarCanais(ctx: Context) {
        val nm = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.createNotificationChannel(NotificationChannel(
            CANAL_PERSISTENTE, "Próxima tarefa", NotificationManager.IMPORTANCE_LOW
        ).apply { description = "Mostra a tarefa mais importante agora"; setShowBadge(false) })
        nm.createNotificationChannel(NotificationChannel(
            CANAL_CHECKIN, "Check-ins diários", NotificationManager.IMPORTANCE_DEFAULT
        ).apply { description = "Lembretes suaves nos horários de trabalho" })
        nm.createNotificationChannel(NotificationChannel(
            CANAL_ALARME, "Alarmes de compromisso", NotificationManager.IMPORTANCE_HIGH
        ).apply { description = "Alarme para compromissos com horário exato" })
        nm.createNotificationChannel(NotificationChannel(
            CANAL_RISCO, "Alerta de prazo", NotificationManager.IMPORTANCE_DEFAULT
        ).apply { description = "Avisa quando o prazo está ficando justo" })
        nm.createNotificationChannel(NotificationChannel(
            CANAL_MAQUINA, "Checkin de máquina", NotificationManager.IMPORTANCE_DEFAULT
        ).apply { description = "Avisa quando a impressora/máquina termina" })
    }

    fun mostrarPersistente(ctx: Context, tarefa: Tarefa?) {
        val nm = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (tarefa == null) { nm.cancel(NOTIF_PERSISTENTE); return }

        val abrirApp = pendingActivity(ctx, 0)
        val base = tarefa.id.hashCode() and 0x7FFFFFFF

        val piConcluir = pendingBroadcast(ctx, base,     NotificationReceiver.ACTION_CONCLUIR, tarefa.id)
        val piAdiar    = pendingBroadcast(ctx, base + 1, NotificationReceiver.ACTION_ADIAR,    tarefa.id)
        val piIniciar  = pendingBroadcast(ctx, base + 2, NotificationReceiver.ACTION_INICIAR,  tarefa.id)
        val piDividir  = pendingBroadcast(ctx, base + 3, NotificationReceiver.ACTION_DIVIDIR,  tarefa.id)

        val risco = NivelRisco.valueOf(tarefa.nivelRisco)
        val prefixoRisco = when (risco) {
            NivelRisco.CRITICO  -> "🔴 "
            NivelRisco.APERTADO -> "🟠 "
            NivelRisco.ATENCAO  -> "🟡 "
            else                -> ""
        }

        val subtitulo = when {
            tarefa.microPasso.isNotBlank()   -> tarefa.microPasso
            tarefa.proximaAcao.isNotBlank()  -> tarefa.proximaAcao
            else -> tarefa.categoria + (tarefa.tempoEstimadoMin?.let { " · ~${it}min" } ?: "")
        }

        nm.notify(
            NOTIF_PERSISTENTE,
            NotificationCompat.Builder(ctx, CANAL_PERSISTENTE)
                .setSmallIcon(R.drawable.ic_notif)
                .setContentTitle("$prefixoRisco${tarefa.titulo}")
                .setContentText(subtitulo)
                .setStyle(NotificationCompat.BigTextStyle().bigText(subtitulo))
                .setContentIntent(abrirApp)
                .setOngoing(true)
                .setShowWhen(false)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .addAction(0, "▶ Começar", piIniciar)
                .addAction(0, "✓ Concluir", piConcluir)
                .addAction(0, "⏱ +30min", piAdiar)
                .addAction(0, "✂ Dividir", piDividir)
                .build()
        )
    }

    fun mostrarCheckin(ctx: Context, tarefa: Tarefa, titulo: String = "Hora de focar") {
        val nm = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val tempo = tarefa.tempoEstimadoMin?.let { " (~${it}min)" } ?: ""
        val detalhe = tarefa.microPasso.ifBlank { tarefa.proximaAcao.ifBlank { tarefa.motivo } }

        nm.notify(NOTIF_CHECKIN,
            NotificationCompat.Builder(ctx, CANAL_CHECKIN)
                .setSmallIcon(R.drawable.ic_notif)
                .setContentTitle(titulo)
                .setContentText("${tarefa.titulo}$tempo")
                .setStyle(NotificationCompat.BigTextStyle()
                    .bigText("${tarefa.titulo}$tempo\n$detalhe".trim()))
                .setContentIntent(pendingActivity(ctx, 10))
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .build()
        )
    }

    fun mostrarAlarmeCompromisso(ctx: Context, tarefa: Tarefa) {
        val nm = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val base = tarefa.id.hashCode() and 0x7FFFFFFF
        val piSoneca5  = pendingBroadcast(ctx, base + 10, NotificationReceiver.ACTION_SONECA_5,  tarefa.id)
        val piSoneca10 = pendingBroadcast(ctx, base + 11, NotificationReceiver.ACTION_SONECA_10, tarefa.id)
        val piConcluir = pendingBroadcast(ctx, base,      NotificationReceiver.ACTION_CONCLUIR,  tarefa.id)

        nm.notify(NOTIF_ALARME,
            NotificationCompat.Builder(ctx, CANAL_ALARME)
                .setSmallIcon(R.drawable.ic_notif)
                .setContentTitle("⏰ Compromisso agora: ${tarefa.titulo}")
                .setContentText(tarefa.microPasso.ifBlank { tarefa.proximaAcao })
                .setContentIntent(pendingActivity(ctx, 20))
                .setAutoCancel(false)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .addAction(0, "Soneca 5min", piSoneca5)
                .addAction(0, "Soneca 10min", piSoneca10)
                .addAction(0, "✓ Concluir", piConcluir)
                .build()
        )
    }

    fun mostrarAlertaRisco(ctx: Context, tarefa: Tarefa, nivel: NivelRisco) {
        val nm = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val (emoji, mensagem) = when (nivel) {
            NivelRisco.CRITICO  -> "🔴" to "O prazo está muito próximo. Vale revisar o que ainda dá tempo."
            NivelRisco.APERTADO -> "🟠" to "O prazo está apertando. É um bom momento para avaliar o andamento."
            NivelRisco.ATENCAO  -> "🟡" to "O prazo está chegando. Dá uma olhada no que falta."
            else -> return
        }

        nm.notify(NOTIF_RISCO,
            NotificationCompat.Builder(ctx, CANAL_RISCO)
                .setSmallIcon(R.drawable.ic_notif)
                .setContentTitle("$emoji ${tarefa.titulo}")
                .setContentText(mensagem)
                .setStyle(NotificationCompat.BigTextStyle().bigText(mensagem))
                .setContentIntent(pendingActivity(ctx, 30))
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .build()
        )
    }

    fun mostrarCheckinMaquina(ctx: Context, tarefa: Tarefa, fase: String) {
        val nm = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(NOTIF_MAQUINA,
            NotificationCompat.Builder(ctx, CANAL_MAQUINA)
                .setSmallIcon(R.drawable.ic_notif)
                .setContentTitle("🖨️ Impressão: $fase")
                .setContentText(tarefa.titulo)
                .setContentIntent(pendingActivity(ctx, 40))
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .build()
        )
    }

    // método antigo mantido como alias para compatibilidade com CheckInWorker existente
    fun mostrarAlertaCheckin(ctx: Context, tarefa: Tarefa) = mostrarCheckin(ctx, tarefa)

    private fun pendingActivity(ctx: Context, requestCode: Int): PendingIntent =
        PendingIntent.getActivity(ctx, requestCode,
            Intent(ctx, MainActivity::class.java).apply { flags = Intent.FLAG_ACTIVITY_SINGLE_TOP },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

    private fun pendingBroadcast(ctx: Context, requestCode: Int, action: String, tarefaId: String): PendingIntent =
        PendingIntent.getBroadcast(ctx, requestCode,
            Intent(ctx, NotificationReceiver::class.java).apply {
                this.action = action
                putExtra(NotificationReceiver.EXTRA_ID, tarefaId)
        },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
}
