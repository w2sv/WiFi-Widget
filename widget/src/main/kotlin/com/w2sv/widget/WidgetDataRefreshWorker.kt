package com.w2sv.widget

import android.content.Context
import android.os.PowerManager
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.w2sv.androidutils.coroutines.getSynchronousMap
import com.w2sv.common.data.sources.WidgetRefreshingParameter
import com.w2sv.common.data.storage.WidgetConfigurationRepository
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
        widgetConfigurationRepository: WidgetConfigurationRepository
    ) {
        private val refreshingParameters =
            widgetConfigurationRepository.refreshingParameters.getSynchronousMap()

        fun applyChangedParameters() {
            when (refreshingParameters.getValue(WidgetRefreshingParameter.RefreshPeriodically)) {
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
                            .setRequiresBatteryNotLow(
                                requiresBatteryNotLow = !refreshingParameters.getValue(
                                    WidgetRefreshingParameter.RefreshOnBatteryLow
                                )
                            )
                            .build()
                    )
                    .setInitialDelay(refreshPeriod)
                    .build()
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