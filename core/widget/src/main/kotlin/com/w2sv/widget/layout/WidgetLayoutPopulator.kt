package com.w2sv.widget.layout

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.RemoteViews
import androidx.annotation.IdRes
import com.w2sv.androidutils.appwidget.crossVisualize
import com.w2sv.androidutils.appwidget.setBackgroundColor
import com.w2sv.androidutils.appwidget.setColorFilter
import com.w2sv.androidutils.graphics.getAlphaSetColor
import com.w2sv.common.AppExtra
import com.w2sv.core.widget.R
import com.w2sv.domain.model.WifiStatus
import com.w2sv.domain.repository.WidgetRepository
import com.w2sv.networking.WifiStatusGetter
import com.w2sv.widget.CopyPropertyToClipboardActivity
import com.w2sv.widget.WifiWidgetProvider
import com.w2sv.widget.data.appearanceBlocking
import com.w2sv.widget.model.WidgetBottomBarElement
import com.w2sv.widget.utils.activityPendingIntent
import com.w2sv.widget.utils.goToWifiSettingsPendingIntent
import com.w2sv.widget.utils.setTextView
import com.w2sv.widget.utils.setViewVisibility
import dagger.hilt.android.qualifiers.ApplicationContext
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

internal class WidgetLayoutPopulator @Inject constructor(
    widgetRepository: WidgetRepository,
    @ApplicationContext private val context: Context,
    private val appWidgetManager: AppWidgetManager,
    private val wifiStatusGetter: WifiStatusGetter
) {
    private val appearance = widgetRepository.appearanceBlocking
    private val colors = appearance.getColors(context)

    fun populate(widget: RemoteViews, appWidgetId: Int): RemoteViews =
        widget
            .apply {
                setContentLayout(
                    appWidgetId = appWidgetId
                )
                setBackgroundColor(
                    id = R.id.widget_layout,
                    color = getAlphaSetColor(colors.background, appearance.backgroundOpacity)
                )
                setBottomBar(bottomBar = appearance.bottomBar, appWidgetId = appWidgetId)
            }

    private fun RemoteViews.setContentLayout(appWidgetId: Int) {
        when (val wifiStatus = wifiStatusGetter()) {
            WifiStatus.Connected -> {
                crossVisualize(
                    R.id.no_connection_available_layout,
                    R.id.wifi_property_list_view
                )

                @Suppress("DEPRECATION")
                setRemoteAdapter(
                    R.id.wifi_property_list_view,
                    Intent(context, WifiPropertyViewsService::class.java)
                        .apply {
                            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                            data = Uri.parse(toUri(Intent.URI_INTENT_SCHEME))
                        }
                )

                setPendingIntentTemplate(
                    R.id.wifi_property_list_view,
                    activityPendingIntent(
                        context,
                        Intent(context, CopyPropertyToClipboardActivity::class.java),
                        PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                    )
                )

                @Suppress("DEPRECATION")
                appWidgetManager
                    .notifyAppWidgetViewDataChanged(appWidgetId, R.id.wifi_property_list_view)
            }

            else -> {
                crossVisualize(
                    R.id.wifi_property_list_view,
                    R.id.no_connection_available_layout
                )

                setTextView(
                    viewId = R.id.wifi_status_tv,
                    text = context.getString(
                        if (wifiStatus == WifiStatus.Disabled) {
                            R.string.wifi_disabled
                        } else {
                            R.string.no_wifi_connection
                        }
                    ),
                    color = colors.secondary
                )

                setOnClickPendingIntent(
                    R.id.no_connection_available_layout,
                    goToWifiSettingsPendingIntent(context)
                )
            }
        }
    }

    // ============
    // Bottom Row
    // ============

    private fun RemoteViews.setBottomBar(bottomBar: WidgetBottomBarElement, appWidgetId: Int) {
        setViewVisibility(R.id.bottom_row, bottomBar.isAnyEnabled) {
            setViewVisibility(R.id.last_updated_tv, bottomBar.lastRefreshTimeDisplay) {
                setTextColor(R.id.last_updated_tv, colors.secondary)

                val now = Date()
                setTextView(
                    viewId = R.id.last_updated_tv,
                    text = "${
                        DateFormat.getTimeInstance(DateFormat.SHORT).format(now)
                    } ${SimpleDateFormat("EE", Locale.getDefault()).format(now)}",
                    size = appearance.fontSize.value
                )
            }

            setButton(
                id = R.id.refresh_button,
                show = bottomBar.refreshButton,
                pendingIntent = PendingIntent.getBroadcast(
                    context,
                    appWidgetId,
                    WifiWidgetProvider.getRefreshDataIntent(context, appWidgetId),
                    PendingIntent.FLAG_IMMUTABLE
                )
            )
            setButton(
                id = R.id.go_to_wifi_settings_button,
                show = bottomBar.goToWifiSettingsButton,
                pendingIntent = goToWifiSettingsPendingIntent(context)
            )
            setButton(
                id = R.id.go_to_widget_settings_button,
                show = bottomBar.goToWidgetSettingsButton,
                pendingIntent = activityPendingIntent(
                    context,
                    Intent.makeRestartActivityTask(
                        ComponentName(
                            context,
                            "com.w2sv.wifiwidget.MainActivity"
                        )
                    )
                        .putExtra(
                            AppExtra.INVOKE_WIDGET_CONFIGURATION_SCREEN,
                            true
                        ),
                    PendingIntent.FLAG_IMMUTABLE
                )
            )
        }
    }

    private fun RemoteViews.setButton(
        @IdRes id: Int,
        show: Boolean,
        pendingIntent: PendingIntent
    ) {
        setViewVisibility(id, show) {
            setColorFilter(id, colors.primary)
            setOnClickPendingIntent(id, pendingIntent)
        }
    }
}
