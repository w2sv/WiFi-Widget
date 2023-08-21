package com.w2sv.widget

import android.content.Context
import android.os.PowerManager
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.w2sv.androidutils.coroutines.getValueSynchronously
import com.w2sv.data.storage.WidgetRepository
import com.w2sv.widget.data.refreshing
import com.w2sv.widget.model.WidgetRefreshing
import slimber.log.i
import java.time.Duration
import javax.inject.Inject

class WidgetDataRefreshWorker(context: Context, workerParams: WorkerParameters) :
    Worker(context, workerParams) {

    override fun doWork(): Result {
        when (applicationContext.getSystemService(PowerManager::class.java).isInteractive) {
            false -> i { "System not interactive; Skipping Widget Data refresh" }
            true -> {
                WidgetProvider.triggerDataRefresh(applicationContext)
                i { "Refreshed Widget Data" }
            }
        }
        return Result.success()
    }

    class Manager @Inject constructor(
        private val workManager: WorkManager,
        private val widgetRepository: WidgetRepository,
    ) {

        private val refreshing: WidgetRefreshing get() = widgetRepository.refreshing.getValueSynchronously()

        fun applyChangedParameters() {
            when (refreshing.refreshPeriodically) {
                true -> enableWorker()
                false -> cancelWorker()
            }
        }

        fun enableWorker() {
            val refreshPeriod = Duration.ofMinutes(15L)

            workManager.enqueueUniquePeriodicWork(
                UNIQUE_WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                PeriodicWorkRequestBuilder<WidgetDataRefreshWorker>(refreshPeriod)
                    .setConstraints(
                        Constraints.Builder()
                            .setRequiresBatteryNotLow(requiresBatteryNotLow = !refreshing.refreshOnLowBattery)
                            .build(),
                    )
                    .setInitialDelay(refreshPeriod)
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
        private val UNIQUE_WORK_NAME get() = WidgetDataRefreshWorker::class.java.simpleName
    }
}
