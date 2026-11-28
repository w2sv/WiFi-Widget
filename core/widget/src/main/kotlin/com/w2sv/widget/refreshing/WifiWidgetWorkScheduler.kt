package com.w2sv.widget.refreshing

import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.ListenableWorker
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.PeriodicWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.w2sv.domain.widget.WidgetRefreshing
import java.time.Duration
import javax.inject.Inject
import kotlin.time.toJavaDuration
import slimber.log.i

internal class WifiWidgetWorkScheduler @Inject constructor(private val workManager: WorkManager) {

    fun applyRefreshingPolicy(settings: WidgetRefreshing) {
        if (settings.refreshPeriodically) {
            enqueuePeriodicWork(settings.interval.toJavaDuration())
        } else {
            cancelPeriodicWork()
        }
    }

    // ------------- Enqueuing

    fun enqueueImmediateRefresh() {
        val request = buildOneTimeWorkRequest<WifiWidgetRefreshWorker> { builder ->
            builder
                .setInputData(RefreshWork.Immediate.toWorkData())
                .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
        }
        i { "Enqueueing widget refresh: reason=${RefreshWork.Immediate}, workId=${request.id}" }

        workManager.enqueueUniqueWork(
            RefreshWork.Immediate.uniqueWorkName,
            ExistingWorkPolicy.REPLACE,
            request
        )
    }

    /** KEEP deduplicates pending work; cache completeness prevents re-enqueue after work finishes. */
    fun enqueueRecovery() {
        // CONNECTED gates retries but may mean VPN/cellular; the worker and repository check
        // Wi-Fi status and the actual default egress context before fetching. Exponential backoff
        // keeps standby and temporarily unavailable networks from causing rapid repeated work.
        val request = buildOneTimeWorkRequest<WifiWidgetRefreshWorker> { builder ->
            builder
                .setInputData(RefreshWork.Recovery.toWorkData())
                .setConstraints(Constraints(requiredNetworkType = NetworkType.CONNECTED))
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, Duration.ofMinutes(10))
        }
        i { "Enqueueing widget refresh: reason=${RefreshWork.Recovery}, workId=${request.id}" }
        workManager.enqueueUniqueWork(RefreshWork.Recovery.uniqueWorkName, ExistingWorkPolicy.KEEP, request)
    }

    private fun enqueuePeriodicWork(interval: Duration) {
        val request = buildPeriodicWorkRequest<WifiWidgetRefreshWorker>(interval) { builder ->
            builder
                .setInputData(RefreshWork.Periodic.toWorkData())
                .setInitialDelay(interval)
        }
        i { "Enqueueing widget refresh: reason=${RefreshWork.Periodic}, workId=${request.id}, interval=$interval" }
        workManager.enqueueUniquePeriodicWork(
            RefreshWork.Periodic.uniqueWorkName,
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }

    // ------------- Cancellation

    fun cancelPeriodicWork() {
        i { "Cancelling periodic work" }
        workManager.cancelUniqueWork(RefreshWork.Periodic.uniqueWorkName)
    }

    fun cancelRecoveryWork() {
        i { "Cancelling remote data recovery work" }
        workManager.cancelUniqueWork(RefreshWork.Recovery.uniqueWorkName)
    }
}

private inline fun <reified W : ListenableWorker> buildOneTimeWorkRequest(
    configure: (OneTimeWorkRequest.Builder) -> Unit
): OneTimeWorkRequest =
    OneTimeWorkRequestBuilder<W>().apply(configure).build()

private inline fun <reified W : ListenableWorker> buildPeriodicWorkRequest(
    repeatInterval: Duration,
    configure: (PeriodicWorkRequest.Builder) -> Unit
): PeriodicWorkRequest =
    PeriodicWorkRequestBuilder<W>(repeatInterval)
        .apply(configure)
        .build()
