package com.w2sv.widget

import android.content.Context
import android.os.PowerManager
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.w2sv.domain.repository.WidgetRepository
import com.w2sv.widget.data.refreshing
import slimber.log.i
import java.time.Duration
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Minimum interval for periodic work.
 */
private val REFRESH_PERIOD = Duration.ofMinutes(15L)

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
        private val workManager: WorkManager,
        private val widgetRepository: WidgetRepository,
    ) {
        fun enableWorkerIfRefreshingEnabled() {
            with(widgetRepository.refreshing) {
                when (refreshPeriodically) {
                    true -> enableWorker(refreshOnLowBattery)
                    false -> cancelWorker()
                }
            }
        }

        private fun enableWorker(refreshOnLowBattery: Boolean) {
            workManager.enqueueUniquePeriodicWork(
                UNIQUE_WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                PeriodicWorkRequestBuilder<WidgetDataRefreshWorker>(REFRESH_PERIOD)
                    .setConstraints(
                        Constraints.Builder()
                            .setRequiresBatteryNotLow(requiresBatteryNotLow = !refreshOnLowBattery)
                            .build(),
                    )
                    .setInitialDelay(REFRESH_PERIOD)
                    .build(),
            )
            i { "Enqueued $UNIQUE_WORK_NAME" }
        }

        fun cancelWorker() {
            workManager
                .cancelUniqueWork(UNIQUE_WORK_NAME)
            i { "Cancelled $UNIQUE_WORK_NAME" }
        }
    }

    companion object {
        private val UNIQUE_WORK_NAME = WidgetDataRefreshWorker::class.java.simpleName
    }
}
