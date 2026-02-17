package com.w2sv.widget

import android.content.Context
import android.os.PowerManager
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.w2sv.domain.repository.RemoteNetworkInfoRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import slimber.log.i

@HiltWorker
internal class WifiWidgetRefreshWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val powerManager: PowerManager,
    private val remoteNetworkInfoRepository: RemoteNetworkInfoRepository
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        if (!powerManager.isInteractive) {
            i { "System not interactive; Skipping Widget Data refresh" }
        } else {
            remoteNetworkInfoRepository.refresh()
            WifiWidgetProvider.triggerDataRefresh(applicationContext)
            i { "Refreshed Widget Data" }
        }
        return Result.success()
    }

    companion object {
        internal const val UNIQUE_WORK_NAME = "WifiWidgetRefreshWorker"
    }
}
