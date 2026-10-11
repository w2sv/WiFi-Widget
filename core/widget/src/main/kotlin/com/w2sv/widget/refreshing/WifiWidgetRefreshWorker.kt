package com.w2sv.widget.refreshing

import android.content.Context
import android.os.PowerManager
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.w2sv.domain.repository.RemoteWifiDataRepository
import com.w2sv.domain.repository.WidgetConfigReader
import com.w2sv.networking.wifistatus.provider.WifiStatusProvider
import com.w2sv.widget.actions.WidgetActions
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import slimber.log.d
import slimber.log.i

/** Refreshes remote data and renders widgets for periodic, manual, and recovery work. */
@HiltWorker
internal class WifiWidgetRefreshWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val powerManager: PowerManager,
    private val remoteWifiDataRepository: RemoteWifiDataRepository,
    private val widgetActions: WidgetActions,
    private val wifiStatusProvider: WifiStatusProvider,
    private val widgetConfigReader: WidgetConfigReader
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
        val config = widgetConfigReader.read()
        val before = remoteWifiDataRepository.data.value

        val result = when {
            !remoteWifiDataRepository.needsRecovery(config) -> Result.success()
            !wifiStatusProvider().isConnected -> Result.retry()
            else -> {
                val refreshResult = remoteWifiDataRepository.refresh(onlyIfMissing = true)
                d { "Widget remote recovery finished: result=$refreshResult" }

                if (refreshResult == Complete) Result.success() else Result.retry()
            }
        }

        if (remoteWifiDataRepository.data.value != before) widgetActions.render()
        return result
    }

    private suspend fun refreshWidget(reason: RefreshWork): Result {
        val wifiStatus = wifiStatusProvider()
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
