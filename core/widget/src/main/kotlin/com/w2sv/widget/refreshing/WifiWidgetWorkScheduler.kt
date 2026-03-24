package com.w2sv.widget.refreshing

import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.w2sv.domain.model.widget.WidgetRefreshing
import java.time.Duration
import javax.inject.Inject
import kotlin.time.toJavaDuration
import slimber.log.i

internal class WifiWidgetWorkScheduler @Inject constructor(private val workManager: WorkManager) {

    fun applyRefreshingPolicy(settings: WidgetRefreshing) {
        if (settings.refreshPeriodically) {
            enqueuePeriodicWork(
                interval = settings.interval.toJavaDuration(),
                refreshOnLowBattery = settings.refreshOnLowBattery
            )
        } else {
            cancelPeriodicWork()
        }
    }

    fun enqueueImmediateRefresh() {
        i { "Enqueueing immediate refresh" }

        val request = OneTimeWorkRequestBuilder<WifiWidgetRefreshWorker>()
            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            .build()

        workManager.enqueueUniqueWork(
            IMMEDIATE_WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            request
        )
    }

    private fun enqueuePeriodicWork(interval: Duration, refreshOnLowBattery: Boolean) {
        i { "Enqueuing periodic work with interval=$interval, refreshOnLowBattery=$refreshOnLowBattery" }

        val request = PeriodicWorkRequestBuilder<WifiWidgetRefreshWorker>(interval)
            .setConstraints(
                Constraints.Builder()
                    .setRequiresBatteryNotLow(requiresBatteryNotLow = !refreshOnLowBattery)
                    .build()
            )
            .setInitialDelay(interval)
            .build()

        workManager.enqueueUniquePeriodicWork(
            PERIODIC_WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }

    fun cancelPeriodicWork() {
        i { "Cancelling periodic work" }
        workManager.cancelUniqueWork(PERIODIC_WORK_NAME)
    }

    private companion object {
        const val PERIODIC_WORK_NAME = "periodic_widget_refreshing"
        const val IMMEDIATE_WORK_NAME = "immediate_widget_refreshing"
    }
}
