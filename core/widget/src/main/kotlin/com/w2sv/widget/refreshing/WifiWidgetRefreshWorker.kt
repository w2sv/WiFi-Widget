package com.w2sv.widget.refreshing

import android.content.Context
import android.os.PowerManager
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.w2sv.domain.networking.NetworkAvailabilitySource
import com.w2sv.domain.networking.RemoteNetworkDataRepository
import com.w2sv.domain.networking.RemoteRefreshResult
import com.w2sv.domain.widget.WidgetConfigSource
import com.w2sv.domain.widget.WidgetManager
import com.w2sv.widget.rendering.WidgetRenderCoordinator
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlin.time.TimeSource
import kotlinx.coroutines.CancellationException
import slimber.log.d
import slimber.log.e
import slimber.log.i

/**
 * Runs manual, periodic, and missing-data recovery work under interactive-state gating.
 *
 * Immediate and periodic work render before HTTP because persisted work can outlive the caller's
 * render signal. Recovery renders only changed published data and retries if incomplete or ineligible.
 *
 * Normal completion awaits Glance dispatch without retrying HTTP on dispatch failure. Cancellation
 * retains changed-data render signals without waiting for dispatch. Dispatch neither confirms
 * launcher display nor guarantees delivery across process death.
 */
@HiltWorker
internal class WifiWidgetRefreshWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val powerManager: PowerManager,
    private val remoteNetworkDataRepository: RemoteNetworkDataRepository,
    private val networkAvailabilitySource: NetworkAvailabilitySource,
    private val widgetManager: WidgetManager,
    private val widgetConfigSource: WidgetConfigSource,
    private val renderCoordinator: WidgetRenderCoordinator
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val started = TimeSource.Monotonic.markNow()
        val reason = RefreshWork.fromWorkData(inputData)
        d { "Widget refresh work started: reason=$reason, workId=$id, attempt=$runAttemptCount" }
        val isInteractive = powerManager.isInteractive

        try {
            val before = remoteNetworkDataRepository.data.value
            val result = try {
                // Dormant connectivity APIs can misreport Wi-Fi. Preserve periodic skip and recovery backoff.
                when {
                    !isInteractive && reason.isRecovery -> Result.retry()
                    !isInteractive -> Result.success()
                    reason.isRecovery -> recover()
                    else -> refreshWidget(reason)
                }
            } finally {
                // This can also detect another refresh's publication while recovery waited for
                // the repository mutex. Preserve the signal if cancellation skips dispatch.
                if (isInteractive && remoteNetworkDataRepository.data.value != before) {
                    renderCoordinator.requestRender()
                }
            }
            if (isInteractive) dispatchPending()
            i { "Widget refresh finished: reason=$reason, workId=$id, result=$result, interactive=$isInteractive" }
            return result
        } finally {
            d { "Widget refresh work ended: reason=$reason, workId=$id, duration=${started.elapsedNow()}" }
        }
    }

    private suspend fun dispatchPending() {
        try {
            renderCoordinator.dispatchPending()
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            e { "Widget refresh dispatch failed: error=$exception" }
        }
    }

    private suspend fun recover(): Result {
        val config = widgetConfigSource.snapshot()
        return when {
            !remoteNetworkDataRepository.needsRecovery(config.remoteDataConfig) -> Result.success()
            !networkAvailabilitySource().canRefreshWidgetRemoteData -> Result.retry()
            else -> {
                val refreshResult = fetchRemote(RefreshWork.Recovery)
                if (refreshResult == RemoteRefreshResult.Complete) Result.success() else Result.retry()
            }
        }
    }

    private suspend fun refreshWidget(reason: RefreshWork): Result {
        // An immediate caller's process may have died before dispatching its initial render.
        widgetManager.render()
        val availability = networkAvailabilitySource()
        if (availability.canRefreshWidgetRemoteData) {
            fetchRemote(reason)
        } else {
            d { "Widget remote refresh skipped: no eligible Wi-Fi or VPN default route" }
        }
        return Result.success()
    }

    private suspend fun fetchRemote(reason: RefreshWork): RemoteRefreshResult {
        val started = TimeSource.Monotonic.markNow()
        d { "Widget remote retrieval started: reason=$reason" }
        try {
            return remoteNetworkDataRepository.refreshForWidget(onlyIfMissing = reason.isRecovery)
                .also { result -> d { "Widget remote retrieval result: reason=$reason, result=$result" } }
        } finally {
            // Includes repository mutex waiting; the repository measures HTTP separately.
            d { "Widget remote retrieval ended: reason=$reason, duration=${started.elapsedNow()}" }
        }
    }
}
