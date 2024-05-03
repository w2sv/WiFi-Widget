package com.w2sv.widget

import android.content.Context
import android.os.PowerManager
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.w2sv.domain.model.WidgetRefreshingParameter
import com.w2sv.widget.model.WidgetRefreshing
import slimber.log.i
import java.time.Duration
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.toJavaDuration

class WidgetDataRefreshWorker(appContext: Context, workerParams: WorkerParameters) :
    Worker(appContext, workerParams) {

    private val powerManager = appContext.getSystemService(PowerManager::class.java)

    override fun doWork(): Result {
        when (powerManager.isInteractive) {
            false -> i { "System not interactive; Skipping Widget Data refresh" }
            true -> {
                WidgetProvider.triggerDataRefresh(applicationContext)
                i { "Refreshed Widget Data" }
            }
        }
        return Result.success()
    }

    @Singleton
    class Manager @Inject constructor(
        private val workManager: WorkManager
    ) {
        internal fun applyRefreshingSettings(widgetRefreshing: WidgetRefreshing) {
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

        fun applyRefreshingSettings(
            parameters: Map<WidgetRefreshingParameter, Boolean>,
            interval: kotlin.time.Duration
        ) {
            applyRefreshingSettings(WidgetRefreshing(parameters = parameters, interval = interval))
        }

        private fun enableWorker(refreshOnLowBattery: Boolean, interval: Duration) {
            workManager.enqueueUniquePeriodicWork(
                UNIQUE_WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                PeriodicWorkRequestBuilder<WidgetDataRefreshWorker>(interval)
                    .setConstraints(
                        Constraints.Builder()
                            .setRequiresBatteryNotLow(requiresBatteryNotLow = !refreshOnLowBattery)
                            .build(),
                    )
                    .setInitialDelay(interval)
                    .build(),
            )
            i { "Enqueued $UNIQUE_WORK_NAME" }
        }

        internal fun cancelWorker() {
            workManager
                .cancelUniqueWork(UNIQUE_WORK_NAME)
            i { "Cancelled $UNIQUE_WORK_NAME" }
        }
    }

    companion object {
        private val UNIQUE_WORK_NAME = WidgetDataRefreshWorker::class.java.simpleName
    }
}
