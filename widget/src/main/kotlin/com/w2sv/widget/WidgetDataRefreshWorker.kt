package com.w2sv.widget

import android.content.Context
import android.os.PowerManager
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import slimber.log.i
import java.time.Duration

internal class WidgetDataRefreshWorker(context: Context, workerParams: WorkerParameters) :
    Worker(context, workerParams) {

    companion object {
        const val UNIQUE_WORK_NAME = "WidgetDataRefreshWorker"

        fun enqueueAsUniquePeriodicWork(
            workManager: WorkManager,
            refreshPeriod: Duration,
            requiresBatteryNotLow: Boolean
        ) {
            workManager.enqueueUniquePeriodicWork(
                UNIQUE_WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                PeriodicWorkRequestBuilder<WidgetDataRefreshWorker>(refreshPeriod)
                    .setConstraints(
                        Constraints.Builder()
                            .setRequiresBatteryNotLow(requiresBatteryNotLow)
                            .build()
                    )
                    .setInitialDelay(refreshPeriod)
                    .build()
            )
        }
    }

    override fun doWork(): Result =
        when (applicationContext.getSystemService(PowerManager::class.java).isInteractive) {
            false -> {
                i { "System not interactive; Skipping Widget Data refresh" }
                Result.failure()
            }

            true -> {
                WifiWidgetProvider.triggerDataRefresh(applicationContext)
                i { "Refreshed Widget Data" }
                Result.success()
            }
        }
}