package com.w2sv.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.w2sv.core.widget.R
import com.w2sv.domain.repository.WidgetRepository
import com.w2sv.widget.data.refreshingBlocking
import com.w2sv.widget.layout.WidgetLayoutPopulator
import com.w2sv.widget.utils.getWifiWidgetIds
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import slimber.log.i

@AndroidEntryPoint
class WifiWidgetProvider : AppWidgetProvider() {

    @Inject
    internal lateinit var widgetDataRefreshWorkerManager: WifiWidgetRefreshWorker.Manager

    @Inject
    internal lateinit var widgetLayoutPopulator: WidgetLayoutPopulator

    @Inject
    internal lateinit var appWidgetManager: AppWidgetManager

    @Inject
    internal lateinit var widgetRepository: WidgetRepository

    /**
     * Called upon the first AppWidget instance being created.
     *
     * Enqueues [WifiWidgetRefreshWorker] as UniquePeriodicWork if not already enqueued.
     */
    override fun onEnabled(context: Context?) {
        super.onEnabled(context)

        i { "onEnabled" }

        widgetDataRefreshWorkerManager.applyRefreshingSettings(widgetRepository.refreshingBlocking)
    }

    /**
     * Called upon last AppWidget instance of provider being deleted.
     *
     * Cancels [WifiWidgetRefreshWorker].
     */
    override fun onDisabled(context: Context?) {
        super.onDisabled(context)

        i { "onDisabled" }

        widgetDataRefreshWorkerManager.cancelWorker()
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        super.onReceive(context, intent) // Required for DI

        i {
            "onReceive | ${intent?.action} | ${
                intent?.extras?.keySet()?.toList()
            } | ${appWidgetManager.getWifiWidgetIds(context!!).toList()}"
        }

        when (intent?.action) {
            ACTION_REFRESH_DATA -> {
                context?.let {
                    // Refresh data
                    onUpdate(
                        it,
                        appWidgetManager,
                        appWidgetManager.getWifiWidgetIds(it)
                    )
                }
            }
        }
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        i { "onUpdate | appWidgetIds=${appWidgetIds.toList()}" }

        val widgetView = RemoteViews(
            context.packageName,
            R.layout.widget
        )

        appWidgetIds.forEach { id ->
            i { "updateWidget | appWidgetId=$id" }

            appWidgetManager.updateAppWidget(
                id,
                widgetLayoutPopulator
                    .populate(
                        widget = widgetView,
                        appWidgetId = id
                    )
            )
        }
    }

    companion object {

        // ===============
        // Refreshing
        // ===============

        fun triggerDataRefresh(context: Context) {
            context.sendBroadcast(getRefreshDataIntent(context))
        }

        fun getRefreshDataIntent(context: Context): Intent =
            Intent(context, WifiWidgetProvider::class.java)
                .setAction(ACTION_REFRESH_DATA)

        private const val ACTION_REFRESH_DATA = "com.w2sv.wifiwidget.action.REFRESH_DATA"
    }
}
