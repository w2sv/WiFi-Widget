package com.w2sv.widget.refreshing

import android.content.Context
import android.os.PowerManager
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.w2sv.domain.repository.RemoteWifiDataRepository
import com.w2sv.networking.wifistatus.provider.WifiStatusProvider
import com.w2sv.widget.actions.WidgetActions
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import slimber.log.i

/**
 * Performs [RemoteWifiDataRepository] data refreshing & triggers widget rendering.
 * Invoked both on refresh button click & periodic refreshing by the [WifiWidgetWorkScheduler].
 */
@HiltWorker
internal class WifiWidgetRefreshWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val powerManager: PowerManager,
    private val remoteWifiDataRepository: RemoteWifiDataRepository,
    private val widgetActions: WidgetActions,
    private val wifiStatusProvider: WifiStatusProvider
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        // Wifi status can only be retrieved if device is interactive
        if (powerManager.isInteractive) {
            refreshWidget()
        }
        return Result.success()
    }

    private suspend fun refreshWidget() {
        i { "Triggering widget refresh" }
        val wifiStatus = wifiStatusProvider()
        if (wifiStatus.isConnected) {
            i { "WifiStatus=$wifiStatus; Refreshing RemoteWifiData" }
            remoteWifiDataRepository.refresh()
        }
        widgetActions.render()
    }
}
