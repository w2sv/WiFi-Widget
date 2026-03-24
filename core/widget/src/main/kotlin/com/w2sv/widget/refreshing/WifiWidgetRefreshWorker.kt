package com.w2sv.widget.refreshing

import android.content.Context
import android.os.PowerManager
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.w2sv.domain.repository.RemoteNetworkInfoRepository
import com.w2sv.widget.actions.WidgetActions
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import slimber.log.i

/**
 * Performs [RemoteNetworkInfoRepository] data refreshing & triggers widget rendering.
 * Invoked both on refresh button click & periodic refreshing by the [WifiWidgetWorkScheduler].
 */
@HiltWorker
internal class WifiWidgetRefreshWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val powerManager: PowerManager,
    private val remoteNetworkInfoRepository: RemoteNetworkInfoRepository,
    private val widgetActions: WidgetActions
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        if (!powerManager.isInteractive) {
            // Wifi status can't be retrieved while device is in standby
            i { "System not interactive; Skipping Widget Data refresh" }
        } else {
            i { "Triggering data refresh" }
            remoteNetworkInfoRepository.refresh()
            widgetActions.render()
        }
        return Result.success()
    }
}
