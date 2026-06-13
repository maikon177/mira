package com.pata3d.mira.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.work.*
import com.pata3d.mira.MiraApplication
import com.pata3d.mira.domain.scoreTarefa
import java.util.Calendar
import java.util.concurrent.TimeUnit

class CheckInWorker(ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {

    override suspend fun doWork(): Result {
        val app  = applicationContext as MiraApplication
        val repo = app.repository
        val prefs = repo.prefsRef

        if (!prefs.notificacoesAtivas) {
            MiraNotificationManager.mostrarPersistente(applicationContext, null)
            return Result.success()
        }

        val agora = System.currentTimeMillis()
        val cal   = Calendar.getInstance()
        val minAtual = cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE)

        val inicio = prefs.silencioInicioMin
        val fim    = prefs.silencioFimMin
        // Silêncio pode cruzar meia-noite (ex: 22:00 → 07:00)
        val emSilencio = if (inicio > fim) minAtual >= inicio || minAtual <= fim
                         else minAtual in inicio..fim

        val tarefas = repo.listarAbertas()
            .filter { it.adiarAte == null || agora >= (it.adiarAte ?: 0L) }

        val melhor = tarefas.maxByOrNull { scoreTarefa(it) }

        MiraNotificationManager.mostrarPersistente(applicationContext, melhor)

        if (!emSilencio && melhor != null && inputData.getString(KEY_TIPO) == TIPO_CHECKIN) {
            MiraNotificationManager.mostrarAlertaCheckin(applicationContext, melhor)
        }

        return Result.success()
    }

    companion object {
        private const val TAG       = "mira_checkin_periodico"
        private const val KEY_TIPO  = "tipo"
        const val TIPO_CHECKIN      = "checkin"
        const val TIPO_REFRESH      = "refresh"

        fun agendar(ctx: Context) {
            val req = PeriodicWorkRequestBuilder<CheckInWorker>(1, TimeUnit.HOURS)
                .addTag(TAG)
                .setInputData(workDataOf(KEY_TIPO to TIPO_CHECKIN))
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 15, TimeUnit.MINUTES)
                .build()

            WorkManager.getInstance(ctx).enqueueUniquePeriodicWork(
                TAG,
                ExistingPeriodicWorkPolicy.KEEP,
                req,
            )
        }

        fun dispararAgora(ctx: Context, tipo: String = TIPO_REFRESH) {
            WorkManager.getInstance(ctx).enqueue(
                OneTimeWorkRequestBuilder<CheckInWorker>()
                    .setInputData(workDataOf(KEY_TIPO to tipo))
                    .build()
            )
        }

        fun agendarCheckins(ctx: Context, manhaMin: Int, tardeMin: Int, noiteMin: Int) {
            val am = ctx.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            listOf(
                "checkin_manha" to manhaMin,
                "checkin_tarde" to tardeMin,
                "checkin_noite" to noiteMin,
            ).forEachIndexed { i, (tag, horarioMin) ->
                val cal = java.util.Calendar.getInstance().apply {
                    set(java.util.Calendar.HOUR_OF_DAY, horarioMin / 60)
                    set(java.util.Calendar.MINUTE, horarioMin % 60)
                    set(java.util.Calendar.SECOND, 0)
                    set(java.util.Calendar.MILLISECOND, 0)
                    if (timeInMillis <= System.currentTimeMillis()) {
                        add(java.util.Calendar.DAY_OF_YEAR, 1)
                    }
                }
                val intent = Intent(ctx, CheckInAlarmReceiver::class.java).apply {
                    putExtra("slot", tag)
                }
                val pi = PendingIntent.getBroadcast(ctx, 100 + i, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
                am.setRepeating(AlarmManager.RTC_WAKEUP, cal.timeInMillis, AlarmManager.INTERVAL_DAY, pi)
            }
        }
    }
}
