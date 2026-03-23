package com.w2sv.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import com.w2sv.androidutils.appwidget.getAppWidgetIds
import com.w2sv.androidutils.content.intent
import com.w2sv.core.widget.R
import com.w2sv.domain.repository.WidgetConfigFlow
import com.w2sv.widget.ui.WidgetRenderer
import com.w2sv.widget.ui.resolve
import com.w2sv.widget.utils.logging.LoggingAppWidgetProvider
import com.w2sv.widget.utils.remoteViews
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import slimber.log.i

@AndroidEntryPoint
class WifiWidgetProvider : LoggingAppWidgetProvider() {

    @Inject
    internal lateinit var refreshManager: WifiWidgetRefreshManager

    @Inject
    internal lateinit var widgetRenderer: WidgetRenderer

    @Inject
    internal lateinit var appWidgetManager: AppWidgetManager

    @Inject
    internal lateinit var widgetConfigFlow: WidgetConfigFlow

    /**
     * Called upon the first AppWidget instance being created.
     *
     * Enqueues [WifiWidgetRefreshWorker] as UniquePeriodicWork if not already enqueued.
     */
    override fun onEnabled(context: Context) {
        super.onEnabled(context)

        val refreshing = runBlocking { widgetConfigFlow.first().refreshing }
        refreshManager.applyRefreshingSettings(refreshing)
    }

    /**
     * Called upon last AppWidget instance of provider being deleted.
     *
     * Cancels [WifiWidgetRefreshWorker].
     */
    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        refreshManager.cancelWorker()
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        when (intent.action) {
            ACTION_REFRESH_DATA -> refreshManager.enqueueImmediateRefresh()
            ACTION_RENDER -> onUpdate(
                context = context,
                appWidgetManager = appWidgetManager,
                appWidgetIds = appWidgetManager.getAppWidgetIds(
                    context.packageName,
                    WifiWidgetProvider::class.java
                )
            )
        }
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)

        val config = runBlocking { widgetConfigFlow.first() }
        val colors = config.appearance.coloring.resolve(context)

        appWidgetIds.forEach { id ->
            i { "updateWidget | appWidgetId=$id" }

            appWidgetManager.updateAppWidget(
                id,
                widgetRenderer(
                    widget = remoteViews(context, R.layout.widget),
                    widgetId = id,
                    config = config,
                    colors = colors
                )
            )
        }
    }

    companion object {
        internal fun render(context: Context) {
            context.sendBroadcast(renderIntent(context))
        }

        private fun renderIntent(context: Context) =
            intent<WifiWidgetProvider>(context).setAction(ACTION_RENDER)

        fun refreshData(context: Context) {
            context.sendBroadcast(refreshDataIntent(context))
        }

        internal fun refreshDataIntent(context: Context): Intent =
            intent<WifiWidgetProvider>(context).setAction(ACTION_REFRESH_DATA)

        private const val ACTION_RENDER = "com.w2sv.wifiwidget.action.RENDER"
        private const val ACTION_REFRESH_DATA = "com.w2sv.wifiwidget.action.REFRESH_DATA"
    }
}
