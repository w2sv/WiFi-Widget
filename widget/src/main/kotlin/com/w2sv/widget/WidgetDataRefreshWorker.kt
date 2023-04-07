package com.w2sv.widget

import android.content.Context
import android.os.PowerManager
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.w2sv.common.preferences.WidgetRefreshingParameters
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import slimber.log.i
import java.time.Duration
import javax.inject.Inject

class WidgetDataRefreshWorker(context: Context, workerParams: WorkerParameters) :
    Worker(context, workerParams) {

    companion object {
        private const val UNIQUE_WORK_NAME = "WidgetDataRefreshWorker"

        private fun enqueueAsUniquePeriodicWork(
            workManager: WorkManager,
            refreshPeriod: Duration,
            requiresBatteryNotLow: Boolean
        ) {
            workManager.enqueueUniquePeriodicWork(
                UNIQUE_WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                PeriodicWorkRequestBuilder<WidgetDataRefreshWorker>(refreshPeriod)
                    .setConstraints(
                        Constraints.Builder()
                            .setRequiresBatteryNotLow(requiresBatteryNotLow)
                            .build()
                    )
                    .setInitialDelay(refreshPeriod)
                    .build()
            )
            i { "Enqueued $UNIQUE_WORK_NAME" }
        }
    }

    class Administrator @Inject constructor(
        @ApplicationContext private val context: Context,
        private val widgetRefreshingParameters: WidgetRefreshingParameters
    ) {

        @InstallIn(SingletonComponent::class)
        @EntryPoint
        interface EntryPointInterface {
            fun getAdministratorInstance(): Administrator
        }

        companion object {
            fun getInstance(context: Context): Administrator =
                EntryPointAccessors.fromApplication(
                    context,
                    EntryPointInterface::class.java
                )
                    .getAdministratorInstance()
        }

        fun applyChangedParameters() {
            when (widgetRefreshingParameters.refreshPeriodically) {
                true -> enableWorker()
                false -> cancelWorker()
            }
        }

        fun enableWorkerIfApplicable() {
            if (widgetRefreshingParameters.refreshPeriodically) {
                enableWorker()
            }
        }

        private fun enableWorker() {
            enqueueAsUniquePeriodicWork(
                WorkManager.getInstance(context),
                Duration.ofMinutes(15L),
                !widgetRefreshingParameters.refreshOnBatteryLow
            )
        }

        fun cancelWorker() {
            WorkManager.getInstance(context)
                .cancelUniqueWork(UNIQUE_WORK_NAME)

            i { "Cancelled $UNIQUE_WORK_NAME" }
        }
    }

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
}