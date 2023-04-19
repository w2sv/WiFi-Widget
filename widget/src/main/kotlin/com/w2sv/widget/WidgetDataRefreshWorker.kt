package com.w2sv.widget

import android.content.Context
import android.os.PowerManager
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.w2sv.common.WidgetRefreshingParameter
import com.w2sv.common.extensions.getDeflowedMap
import com.w2sv.common.datastore.DataStoreRepository
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
        dataStoreRepository: DataStoreRepository
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

        private val widgetRefreshingParameters =
            dataStoreRepository.widgetRefreshingParameters.getDeflowedMap()

        fun applyChangedParameters() {
            when (widgetRefreshingParameters.getValue(WidgetRefreshingParameter.RefreshPeriodically)) {
                true -> enableWorker()
                false -> cancelWorker()
            }
        }

        fun enableWorkerIfApplicable() {
            if (widgetRefreshingParameters.getValue(WidgetRefreshingParameter.RefreshPeriodically)) {
                enableWorker()
            }
        }

        private fun enableWorker() {
            enqueueAsUniquePeriodicWork(
                WorkManager.getInstance(context),
                Duration.ofMinutes(15L),
                !widgetRefreshingParameters.getValue(WidgetRefreshingParameter.RefreshOnBatteryLow)
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