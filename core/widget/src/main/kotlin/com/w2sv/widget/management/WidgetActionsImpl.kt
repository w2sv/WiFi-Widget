package com.w2sv.widget.management

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.Context
import androidx.glance.appwidget.GlanceAppWidgetManager
import com.w2sv.androidutils.appwidget.appWidgetIds
import com.w2sv.androidutils.content.intent
import com.w2sv.common.android.broadcastPendingIntent
import com.w2sv.domain.widget.WidgetActions
import com.w2sv.domain.widget.WidgetConfig
import com.w2sv.domain.widget.WidgetConfigSource
import com.w2sv.widget.WifiWidgetProvider
import com.w2sv.widget.preview.WifiPinPreviewWidget
import com.w2sv.widget.preview.pinPreviewSize
import com.w2sv.widget.preview.widgetPreviewState
import com.w2sv.widget.refreshing.WifiWidgetWorkScheduler
import com.w2sv.widget.rendering.WidgetRenderCoordinator
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import slimber.log.w

internal class WidgetActionsImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val appWidgetManager: AppWidgetManager,
    private val refreshManager: WifiWidgetWorkScheduler,
    private val renderCoordinator: WidgetRenderCoordinator,
    private val configSource: WidgetConfigSource
) : WidgetActions {
    override suspend fun pin(): Boolean =
        appWidgetManager.isRequestPinAppWidgetSupported &&
            try {
                val config = configSource.snapshot()
                val preview = WifiPinPreviewWidget(widgetPreviewState(context, config))
                GlanceAppWidgetManager(context).requestPinGlanceAppWidget(
                    receiver = WifiWidgetProvider::class.java,
                    preview = preview,
                    previewSize = pinPreviewSize,
                    successCallback = broadcastPendingIntent(
                        context,
                        intent<WidgetPinSuccessBroadcastReceiver>(context),
                        PendingIntent.FLAG_IMMUTABLE
                    )
                )
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                w { "Failed requesting widget pin: error=$exception" }
                false
            }

    override fun refresh() {
        context.sendBroadcast(WifiWidgetProvider.refreshIntent(context))
    }

    override fun render() {
        renderCoordinator.requestRender()
    }

    override fun requestUpdate(appWidgetId: Int) {
        context.sendBroadcast(
            intent<WifiWidgetProvider>(context)
                .setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE)
                .putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, intArrayOf(appWidgetId))
        )
    }

    override fun applyConfigurationChange(previous: WidgetConfig, current: WidgetConfig) {
        if (previous.refreshing != current.refreshing && appWidgetManager.appWidgetIds<WifiWidgetProvider>(context).isNotEmpty()) {
            refreshManager.applyRefreshingPolicy(current.refreshing)
        }

        // Trigger an immediate render unblocked by remote data refresh
        render()

        if (previous.remoteDataConfig != current.remoteDataConfig) {
            refresh()
        }
    }
}
