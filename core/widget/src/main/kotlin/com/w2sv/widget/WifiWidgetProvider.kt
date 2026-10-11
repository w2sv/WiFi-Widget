package com.w2sv.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import com.w2sv.androidutils.appwidget.appWidgetIds
import com.w2sv.androidutils.content.intent
import com.w2sv.core.widget.R
import com.w2sv.domain.model.widget.WidgetConfig
import com.w2sv.domain.repository.RemoteWifiDataRepository
import com.w2sv.domain.repository.WidgetConfigSource
import com.w2sv.widget.refreshing.WifiWidgetWorkScheduler
import com.w2sv.widget.ui.container.WidgetRenderer
import com.w2sv.widget.ui.resolve
import com.w2sv.widget.utils.logging.LoggingAppWidgetProvider
import com.w2sv.widget.utils.remoteViews
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
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
    lateinit var widgetConfigSource: WidgetConfigSource

    @Inject
    lateinit var remoteWifiDataRepository: RemoteWifiDataRepository

    /** Applies periodic refresh settings when the first widget is added. */
    override fun onEnabled(context: Context) {
        super.onEnabled(context)

        val refreshing = widgetConfigSource.blockingSnapshot().refreshing
        refreshManager.applyRefreshingPolicy(refreshing)
    }

    /** Cancels periodic and recovery work when the last widget is removed. */
    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        refreshManager.cancelPeriodicWork()
        refreshManager.cancelRecoveryWork()
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        when (intent.action) {
            ACTION_REFRESH -> refreshManager.enqueueImmediateRefresh()
            // ACTION_RENDER must not re-enter onUpdate: a finished recovery work item can be
            // enqueued again despite WorkManager KEEP, creating a render/recovery loop.
            ACTION_RENDER -> renderWidgets(
                context = context,
                appWidgetManager = appWidgetManager,
                appWidgetIds = appWidgetManager.appWidgetIds<WifiWidgetProvider>(context),
                config = widgetConfigSource.blockingSnapshot()
            )
        }
    }

    /**
     * Called when Android requests a widget update via [AppWidgetManager.ACTION_APPWIDGET_UPDATE], typically when
     * a widget is added, after device reboot or restoration. It is not automatically called when the app process restarts.
     * Periodic system updates are disabled (`updatePeriodMillis = 0`);
     * Those are triggered via the WifiWidgetRefreshWorker, as are manual refresh actions.
     *
     * Renders the affected widget instances and schedules recovery of missing remote data,
     * including after process recreation when the application observer has no prior emission
     * to trigger a render. Recovery publication later arrives through the observer's render path.
     */
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)

        val config = widgetConfigSource.blockingSnapshot()
        if (remoteWifiDataRepository.needsRecovery(config.remoteDataConfig)) {
            refreshManager.enqueueRecovery()
        }
        renderWidgets(context, appWidgetManager, appWidgetIds, config)
    }

    internal fun renderWidgets(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
        config: WidgetConfig
    ) {
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
