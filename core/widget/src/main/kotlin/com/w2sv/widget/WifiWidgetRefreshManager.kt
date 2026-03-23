package com.w2sv.widget

import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.w2sv.domain.model.widget.WidgetRefreshing
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.toJavaDuration
import slimber.log.i

@Singleton
class WifiWidgetRefreshManager @Inject constructor(private val workManager: WorkManager) {

    fun applyRefreshingSettings(settings: WidgetRefreshing) {
        when (settings.refreshPeriodically) {
            true -> enableWorker(
                refreshOnLowBattery = settings.refreshOnLowBattery,
                interval = settings.interval.toJavaDuration()
            )

            false -> cancelWorker()
        }
    }

    internal fun enqueueImmediateRefresh() {
        i { "Enqueueing immediate refresh" }

        val request = OneTimeWorkRequestBuilder<WifiWidgetRefreshWorker>()
            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            .build()

        workManager.enqueueUniqueWork(
            "manual_refresh",
            ExistingWorkPolicy.REPLACE,
            request
        )
    }

    private fun enableWorker(refreshOnLowBattery: Boolean, interval: java.time.Duration) {
        i { "Enqueuing ${WifiWidgetRefreshWorker.UNIQUE_WORK_NAME}" }

        val request = PeriodicWorkRequestBuilder<WifiWidgetRefreshWorker>(interval)
            .setConstraints(
                Constraints.Builder()
                    .setRequiresBatteryNotLow(requiresBatteryNotLow = !refreshOnLowBattery)
                    .build()
            )
            .setInitialDelay(interval)
            .build()

        workManager.enqueueUniquePeriodicWork(
            WifiWidgetRefreshWorker.UNIQUE_WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }

    internal fun cancelWorker() {
        i { "Cancelling ${WifiWidgetRefreshWorker.UNIQUE_WORK_NAME}" }

        workManager.cancelUniqueWork(WifiWidgetRefreshWorker.UNIQUE_WORK_NAME)
    }
}
