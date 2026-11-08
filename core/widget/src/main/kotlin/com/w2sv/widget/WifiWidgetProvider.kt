package com.w2sv.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import com.w2sv.androidutils.content.intent
import com.w2sv.domain.networking.NetworkAvailabilitySource
import com.w2sv.domain.networking.RemoteNetworkDataRepository
import com.w2sv.domain.widget.WidgetConfigSource
import com.w2sv.widget.refreshing.WifiWidgetWorkScheduler
import com.w2sv.widget.ui.WifiGlanceWidget
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/** Keeps the installed provider component and its recovery lifecycle while Glance owns updates. */
@AndroidEntryPoint
internal class WifiWidgetProvider : GlanceAppWidgetReceiver() {
    override val glanceAppWidget = WifiGlanceWidget()

    @Inject lateinit var refreshManager: WifiWidgetWorkScheduler

    @Inject lateinit var widgetConfigSource: WidgetConfigSource

    @Inject lateinit var remoteNetworkDataRepository: RemoteNetworkDataRepository

    @Inject lateinit var networkAvailabilitySource: NetworkAvailabilitySource

    @Inject lateinit var remoteDataWidgetObserver: RemoteDataWidgetObserver

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        refreshManager.applyRefreshingPolicy(widgetConfigSource.blockingSnapshot().refreshing)
        remoteDataWidgetObserver.onWidgetsAvailable()
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        refreshManager.cancelPeriodicWork()
        refreshManager.cancelRecoveryWork()
        remoteDataWidgetObserver.onLastWidgetRemoved()
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // Glance owns the asynchronous update and its broadcast lifecycle.
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        reconcileUpdate(appWidgetIds)
    }

    internal fun reconcileUpdate(appWidgetIds: IntArray) {
        if (appWidgetIds.isEmpty()) return
        remoteDataWidgetObserver.onWidgetsAvailable()
        val config = widgetConfigSource.blockingSnapshot()
        if (remoteNetworkDataRepository.needsRecovery(config.remoteDataConfig) &&
            networkAvailabilitySource().canRefreshWidgetRemoteData
        ) {
            refreshManager.enqueueRecovery()
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_REFRESH) refreshManager.enqueueImmediateRefresh()
    }

    companion object {
        internal fun refreshIntent(context: Context): Intent =
            intent<WifiWidgetProvider>(context).setAction(ACTION_REFRESH)

        private const val ACTION_REFRESH = "com.w2sv.wifiwidget.action.REFRESH_DATA"
    }
}
