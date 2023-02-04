package com.w2sv.widget

import android.content.Context
import android.os.PowerManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import slimber.log.i

class WidgetDataRefreshWorker(context: Context, workerParams: WorkerParameters) :
    Worker(context, workerParams) {

    companion object {
        const val UNIQUE_WORK_NAME = "WidgetDataRefreshWorker"
    }

    override fun doWork(): Result {
        when (applicationContext.getSystemService(PowerManager::class.java).isInteractive) {
            false -> i { "System not interactive; Skipping Widget Data refresh" }
            true -> {
                WifiWidgetProvider.triggerDataRefresh(applicationContext)
                i { "Refreshed Widget Data" }
            }
        }
        return Result.success()
    }
}