package com.w2sv.widget.refreshing

import android.content.Context
import android.os.PowerManager
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.w2sv.domain.networking.RemoteRefreshResult
import com.w2sv.domain.networking.RemoteWifiDataRepository
import com.w2sv.domain.networking.WifiStatusSource
import com.w2sv.domain.widget.WidgetActions
import com.w2sv.domain.widget.WidgetConfigSource
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import slimber.log.d
import slimber.log.i

/**
 * Executes periodic, manual, and recovery refreshes.
 *
 * Manual and periodic refreshes explicitly render widgets because local Wi-Fi
 * properties may change independently of remote data.
 *
 * Recovery additionally renders when published remote data changes, ensuring
 * the render request is dispatched before work completes. The application-scoped
 * observer independently handles remote-data changes from other callers.
 *
 * Interactive-state gating avoids unreliable dormant connectivity observations.
 */
@HiltWorker
internal class WifiWidgetRefreshWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val powerManager: PowerManager,
    private val remoteWifiDataRepository: RemoteWifiDataRepository,
    private val widgetActions: WidgetActions,
    private val wifiStatusSource: WifiStatusSource,
    private val widgetConfigSource: WidgetConfigSource
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val reason = RefreshWork.fromWorkData(inputData)
        val isInteractive = powerManager.isInteractive

        // Dormant connectivity APIs can misreport Wi-Fi. Periodic work keeps its old skip
        // behavior; recovery retries with WorkManager backoff until the device can initialize.
        val result = when {
            !isInteractive && reason.isRecovery -> Result.retry()
            !isInteractive -> Result.success()
            reason.isRecovery -> recover()
            else -> refreshWidget(reason)
        }
        i { "Widget refresh finished: reason=$reason, result=$result, interactive=$isInteractive" }
        return result
    }

    private suspend fun recover(): Result {
        val config = widgetConfigSource.snapshot()
        val before = remoteWifiDataRepository.data.value

        try {
            return when {
                !remoteWifiDataRepository.needsRecovery(config.remoteDataConfig) -> Result.success()
                !wifiStatusSource.snapshot().isConnected -> Result.retry()
                else -> {
                    val refreshResult = remoteWifiDataRepository.refresh(onlyIfMissing = true)
                    d { "Widget remote recovery finished: result=$refreshResult" }

                    if (refreshResult == RemoteRefreshResult.Complete) Result.success() else Result.retry()
                }
            }
        } finally {
            // Ensure recovery-triggered changes are rendered before the worker completes,
            // independently of the application-scoped observer.
            if (remoteWifiDataRepository.data.value != before) {
                widgetActions.render()
            }
        }
    }

    private suspend fun refreshWidget(reason: RefreshWork): Result {
        val wifiStatus = wifiStatusSource.snapshot()
        if (wifiStatus.isConnected) {
            d { "Widget remote refresh started: reason=$reason, wifiStatus=$wifiStatus" }
            val refreshResult = remoteWifiDataRepository.refresh()
            d { "Widget remote refresh finished: reason=$reason, result=$refreshResult" }
        } else {
            d { "Widget remote refresh skipped: wifiStatus=$wifiStatus" }
        }
        widgetActions.render()
        return Result.success()
    }
}
