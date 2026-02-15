package com.w2sv.widget

import android.content.Context
import android.os.PowerManager
import androidx.hilt.work.HiltWorker
import androidx.work.Worker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import slimber.log.i

@HiltWorker
internal class WifiWidgetRefreshWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val powerManager: PowerManager
) : Worker(context, params) {

    override fun doWork(): Result {
        if (!powerManager.isInteractive) {
            i { "System not interactive; Skipping Widget Data refresh" }
        } else {
            WifiWidgetProvider.triggerDataRefresh(applicationContext)
            i { "Refreshed Widget Data" }
        }
        return Result.success()
    }

    companion object {
        internal const val UNIQUE_WORK_NAME = "WifiWidgetRefreshWorker"
    }
}
