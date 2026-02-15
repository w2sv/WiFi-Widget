package com.w2sv.widget

import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.w2sv.domain.model.WidgetRefreshing
import slimber.log.i
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.toJavaDuration

@Singleton
class WifiWidgetRefreshManager @Inject constructor(private val workManager: WorkManager) {

    fun applyRefreshingSettings(widgetRefreshing: WidgetRefreshing) {
        with(widgetRefreshing) {
            when (refreshPeriodically) {
                true -> enableWorker(
                    refreshOnLowBattery = refreshOnLowBattery,
                    interval = refreshInterval.toJavaDuration()
                )

                false -> cancelWorker()
            }
        }
    }

    private fun enableWorker(refreshOnLowBattery: Boolean, interval: java.time.Duration) {
        workManager.enqueueUniquePeriodicWork(
            WifiWidgetRefreshWorker.UNIQUE_WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            PeriodicWorkRequestBuilder<WifiWidgetRefreshWorker>(interval)
                .setConstraints(
                    Constraints.Builder()
                        .setRequiresBatteryNotLow(requiresBatteryNotLow = !refreshOnLowBattery)
                        .build()
                )
                .setInitialDelay(interval)
                .build()
        )
        i { "Enqueued ${WifiWidgetRefreshWorker.UNIQUE_WORK_NAME}" }
    }

    internal fun cancelWorker() {
        workManager.cancelUniqueWork(WifiWidgetRefreshWorker.UNIQUE_WORK_NAME)
        i { "Cancelled ${WifiWidgetRefreshWorker.UNIQUE_WORK_NAME}" }
    }
}
