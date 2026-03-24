package com.w2sv.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import com.w2sv.androidutils.appwidget.appWidgetIds
import com.w2sv.androidutils.content.intent
import com.w2sv.core.widget.R
import com.w2sv.domain.repository.WidgetConfigFlow
import com.w2sv.widget.refreshing.WifiWidgetWorkScheduler
import com.w2sv.widget.ui.container.WidgetRenderer
import com.w2sv.widget.ui.resolve
import com.w2sv.widget.utils.logging.LoggingAppWidgetProvider
import com.w2sv.widget.utils.remoteViews
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import slimber.log.i

@AndroidEntryPoint
internal class WifiWidgetProvider : LoggingAppWidgetProvider() {

    @Inject
    lateinit var refreshManager: WifiWidgetWorkScheduler

    @Inject
    lateinit var widgetRenderer: WidgetRenderer

    @Inject
    lateinit var appWidgetManager: AppWidgetManager

    @Inject
    lateinit var widgetConfigFlow: WidgetConfigFlow

    /**
     * Called upon the first AppWidget instance being created.
     * Enqueues [com.w2sv.widget.refreshing.WifiWidgetRefreshWorker] if not already enqueued.
     */
    override fun onEnabled(context: Context) {
        super.onEnabled(context)

        val refreshing = runBlocking { widgetConfigFlow.first().refreshing }
        refreshManager.applyRefreshingPolicy(refreshing)
    }

    /**
     * Called upon last provider AppWidget instance being deleted.
     * Cancels [com.w2sv.widget.refreshing.WifiWidgetRefreshWorker].
     */
    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        refreshManager.cancelPeriodicWork()
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        when (intent.action) {
            ACTION_REFRESH -> refreshManager.enqueueImmediateRefresh()
            ACTION_RENDER -> onUpdate(
                context = context,
                appWidgetManager = appWidgetManager,
                appWidgetIds = appWidgetManager.appWidgetIds<WifiWidgetProvider>(context)
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

        internal fun renderIntent(context: Context) =
            intent<WifiWidgetProvider>(context).setAction(ACTION_RENDER)

        internal fun refreshIntent(context: Context): Intent =
            intent<WifiWidgetProvider>(context).setAction(ACTION_REFRESH)

        private const val ACTION_RENDER = "com.w2sv.wifiwidget.action.RENDER"
        private const val ACTION_REFRESH = "com.w2sv.wifiwidget.action.REFRESH_DATA"
    }
}
