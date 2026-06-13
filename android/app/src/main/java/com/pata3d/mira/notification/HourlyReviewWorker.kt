package com.pata3d.mira.notification

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.pata3d.mira.MiraApplication
import java.util.concurrent.TimeUnit

class HourlyReviewWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val app = applicationContext as? MiraApplication ?: return Result.success()
        if (!app.prefs.notificacoesAtivas) return Result.success()

        val suggestion = app.miraBrain.reviewHourly() ?: return Result.success()
        MiraNotificationManager.mostrarSugestao(applicationContext, suggestion)
        return Result.success()
    }

    companion object {
        const val WORK_NAME = "mira_hourly_review"

        fun agendar(context: Context) {
            val request = PeriodicWorkRequestBuilder<HourlyReviewWorker>(1, TimeUnit.HOURS).build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME, ExistingPeriodicWorkPolicy.KEEP, request,
            )
        }
    }
}
