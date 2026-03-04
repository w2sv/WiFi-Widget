package com.w2sv.widget.layout

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import androidx.annotation.ColorInt
import androidx.annotation.IdRes
import androidx.core.net.toUri
import com.w2sv.androidutils.appwidget.crossVisualize
import com.w2sv.androidutils.appwidget.setBackgroundColor
import com.w2sv.androidutils.appwidget.setColorFilter
import com.w2sv.androidutils.content.intent
import com.w2sv.androidutils.graphics.getAlphaSetColor
import com.w2sv.common.AppAction
import com.w2sv.core.widget.R
import com.w2sv.domain.model.networking.WifiStatus
import com.w2sv.domain.model.widget.FontSize
import com.w2sv.domain.model.widget.WidgetUtility
import com.w2sv.domain.model.widget.WifiWidgetConfig
import com.w2sv.networking.wifistatus.WifiStatusGetter
import com.w2sv.widget.CopyPropertyToClipboardActivity
import com.w2sv.widget.WifiWidgetProvider
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

internal class WidgetRenderer @Inject constructor(
    @ApplicationContext private val context: Context,
    private val appWidgetManager: AppWidgetManager,
    private val getWifiStatus: WifiStatusGetter
) {
    operator fun invoke(widget: RemoteViews, appWidgetId: Int, config: WifiWidgetConfig, colors: WidgetColors): RemoteViews =
        widget.apply {
            setContentLayout(appWidgetId = appWidgetId, colors = colors, fontSize = config.appearance.fontSize)
            setBackgroundColor(
                id = R.id.widget_layout,
                color = getAlphaSetColor(colors.background, config.appearance.backgroundOpacity)
            )
            setBottomBar(
                utilities = config.enabledUtilities(),
                appWidgetId = appWidgetId,
                colors = colors,
                fontSize = config.appearance.fontSize
            )
        }

    private fun RemoteViews.setContentLayout(appWidgetId: Int, colors: WidgetColors, fontSize: FontSize) {
        when (val wifiStatus = getWifiStatus()) {
            WifiStatus.Connected -> {
                crossVisualize(
                    R.id.no_connection_available_layout,
                    R.id.wifi_property_list_view
                )

                @Suppress("DEPRECATION")
                setRemoteAdapter(
                    R.id.wifi_property_list_view,
                    intent<WifiPropertyViewsService>(context).apply {
                        putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                        data = toUri(Intent.URI_INTENT_SCHEME).toUri()
                    }
                )

                setPendingIntentTemplate(
                    R.id.wifi_property_list_view,
                    activityPendingIntent(
                        context,
                        intent<CopyPropertyToClipboardActivity>(context),
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
                    color = colors.secondary,
                    size = fontSize.value + 2
                )

                setOnClickPendingIntent(
                    R.id.no_connection_available_layout,
                    goToWifiSettingsPendingIntent(context)
                )
            }
        }
    }

    // ============
    // Bottom Bar
    // ============

    private fun RemoteViews.setBottomBar(utilities: List<WidgetUtility>, appWidgetId: Int, colors: WidgetColors, fontSize: FontSize) {
        setViewVisibility(R.id.bottom_row, utilities.isNotEmpty()) {
            setViewVisibility(R.id.last_updated_tv, WidgetUtility.LastRefreshTimeDisplay in utilities) {
                setTextColor(R.id.last_updated_tv, colors.secondary)

                val now = Date()
                setTextView(
                    viewId = R.id.last_updated_tv,
                    text = "${
                        DateFormat.getTimeInstance(DateFormat.SHORT).format(now)
                    } ${SimpleDateFormat("EE", Locale.getDefault()).format(now)}",
                    size = fontSize.value
                )
            }

            setButton(
                id = R.id.refresh_button,
                show = WidgetUtility.RefreshButton in utilities,
                color = colors.primary,
                pendingIntent = PendingIntent.getBroadcast(
                    context,
                    appWidgetId,
                    WifiWidgetProvider.getRefreshDataIntent(context, appWidgetId),
                    PendingIntent.FLAG_IMMUTABLE
                )
            )
            setButton(
                id = R.id.go_to_wifi_settings_button,
                show = WidgetUtility.GoToWifiSettingsButton in utilities,
                color = colors.primary,
                pendingIntent = goToWifiSettingsPendingIntent(context)
            )
            setButton(
                id = R.id.go_to_widget_settings_button,
                show = WidgetUtility.GoToWidgetSettingsButton in utilities,
                color = colors.primary,
                pendingIntent = activityPendingIntent(
                    context,
                    Intent.makeRestartActivityTask(
                        ComponentName(
                            context,
                            "com.w2sv.wifiwidget.MainActivity"
                        )
                    )
                        .setAction(AppAction.OPEN_WIDGET_CONFIGURATION_SCREEN),
                    PendingIntent.FLAG_IMMUTABLE
                )
            )
        }
    }
}

private fun RemoteViews.setButton(
    @IdRes id: Int,
    show: Boolean,
    @ColorInt color: Int,
    pendingIntent: PendingIntent
) {
    setViewVisibility(id, show) {
        setColorFilter(id, color)
        setOnClickPendingIntent(id, pendingIntent)
    }
}
