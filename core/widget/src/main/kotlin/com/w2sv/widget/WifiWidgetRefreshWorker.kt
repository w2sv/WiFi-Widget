package com.w2sv.widget

import android.content.Context
import android.os.PowerManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.w2sv.androidutils.service.systemService
import slimber.log.i

internal class WifiWidgetRefreshWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {

    private val powerManager = context.systemService<PowerManager>()

    override fun doWork(): Result {
        if (!powerManager.isInteractive) {
            i { "System not interactive; Skipping Widget Data refresh" }
        }
        else {
            WifiWidgetProvider.triggerDataRefresh(applicationContext)
            i { "Refreshed Widget Data" }
        }
        return Result.success()
    }

    companion object {
        internal val UNIQUE_WORK_NAME = WifiWidgetRefreshWorker::class.java.simpleName
    }
}
