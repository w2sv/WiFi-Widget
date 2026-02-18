package com.w2sv.widget

import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.w2sv.domain.model.widget.WidgetRefreshing
import slimber.log.i
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.toJavaDuration

@Singleton
class WifiWidgetRefreshManager @Inject constructor(private val workManager: WorkManager) {

    fun applyRefreshingSettings(settings: WidgetRefreshing) {
        when (settings.refreshPeriodically) {
            true -> enableWorker(
                refreshOnLowBattery = settings.refreshOnLowBattery,
                interval = settings.refreshInterval.toJavaDuration()
            )

            false -> cancelWorker()
        }
    }

    private fun enableWorker(refreshOnLowBattery: Boolean, interval: java.time.Duration) {
        i { "Enqueuing ${WifiWidgetRefreshWorker.UNIQUE_WORK_NAME}" }
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
    }

    internal fun cancelWorker() {
        i { "Cancelling ${WifiWidgetRefreshWorker.UNIQUE_WORK_NAME}" }
        workManager.cancelUniqueWork(WifiWidgetRefreshWorker.UNIQUE_WORK_NAME)
    }
}
