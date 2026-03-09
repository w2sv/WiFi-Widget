package com.w2sv.widget.ui

import android.appwidget.AppWidgetManager
import android.content.Context
import android.widget.RemoteViews
import com.w2sv.androidutils.appwidget.crossVisualize
import com.w2sv.androidutils.appwidget.setBackgroundColor
import com.w2sv.androidutils.graphics.getAlphaSetColor
import com.w2sv.core.widget.R
import com.w2sv.domain.model.networking.WifiStatus
import com.w2sv.domain.model.widget.FontSize
import com.w2sv.domain.model.widget.WidgetColors
import com.w2sv.domain.model.widget.WidgetUtility
import com.w2sv.domain.model.widget.WifiWidgetConfig
import com.w2sv.networking.wifistatus.WifiStatusGetter
import com.w2sv.widget.CopyPropertyToClipboardActivity
import com.w2sv.widget.WidgetAction
import com.w2sv.widget.ui.properties.WifiPropertyViewsService
import com.w2sv.widget.utils.setButton
import com.w2sv.widget.utils.setImageView
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
    operator fun invoke(
        widget: RemoteViews,
        widgetId: Int,
        config: WifiWidgetConfig,
        colors: WidgetColors
    ): RemoteViews =
        widget.apply {
            val status = getWifiStatus()
            when (status.isConnected) {
                true -> setConnectedContentLayout(widgetId)
                false -> setUnconnectedContentLayout(status = status, colors = colors, fontSize = config.appearance.fontSize)
            }
            setBackgroundColor(
                id = R.id.widget_layout,
                color = getAlphaSetColor(colors.background, config.appearance.backgroundOpacity)
            )
            setBottomBar(
                show = config.enabledUtilities().isNotEmpty(),
                isUtilityEnabled = config.utilities::getValue,
                widgetId = widgetId,
                colors = colors,
                fontSize = config.appearance.fontSize
            )
        }

    // ============
    // Content Layout
    // ============

    private fun RemoteViews.setConnectedContentLayout(widgetId: Int) {
        crossVisualize(
            R.id.no_connection_available_layout,
            R.id.wifi_property_list_view
        )

        @Suppress("DEPRECATION")
        setRemoteAdapter(
            R.id.wifi_property_list_view,
            WifiPropertyViewsService.intent(context, widgetId)
        )

        setPendingIntentTemplate(
            R.id.wifi_property_list_view,
            CopyPropertyToClipboardActivity.pendingIntent(context)
        )

        @Suppress("DEPRECATION")
        appWidgetManager.notifyAppWidgetViewDataChanged(widgetId, R.id.wifi_property_list_view)
    }

    private fun RemoteViews.setUnconnectedContentLayout(
        status: WifiStatus,
        colors: WidgetColors,
        fontSize: FontSize
    ) {
        crossVisualize(
            R.id.wifi_property_list_view,
            R.id.no_connection_available_layout
        )

        setImageView(R.id.wifi_status_icon, status.iconRes, colors.secondary)
        setTextView(
            viewId = R.id.wifi_status_tv,
            text = context.getString(
                if (status == WifiStatus.Disabled) {
                    R.string.wifi_disabled
                } else {
                    R.string.no_wifi_connection
                }
            ),
            color = colors.secondary,
            size = fontSize.value
        )

        setOnClickPendingIntent(
            R.id.no_connection_available_layout,
            WidgetAction.openWifiSettings(context)
        )
    }

    // ============
    // Bottom Bar
    // ============

    private fun RemoteViews.setBottomBar(
        show: Boolean,
        isUtilityEnabled: (WidgetUtility) -> Boolean,
        widgetId: Int,
        colors: WidgetColors,
        fontSize: FontSize
    ) {
        setViewVisibility(R.id.bottom_row, show)
        if (!show) return

        setTextView(
            viewId = R.id.last_updated_tv,
            isVisible = isUtilityEnabled(WidgetUtility.LastRefreshTimeDisplay),
            text = formattedDateTime(),
            size = fontSize.value
        )
        setButton(
            viewId = R.id.refresh_button,
            isVisible = isUtilityEnabled(WidgetUtility.RefreshButton),
            color = colors.primary,
            pendingIntent = WidgetAction.refreshWidget(context, widgetId)
        )
        setButton(
            viewId = R.id.go_to_wifi_settings_button,
            isVisible = isUtilityEnabled(WidgetUtility.GoToWifiSettingsButton),
            color = colors.primary,
            pendingIntent = WidgetAction.openWifiSettings(context)
        )
        setButton(
            viewId = R.id.go_to_widget_settings_button,
            isVisible = isUtilityEnabled(WidgetUtility.GoToWidgetSettingsButton),
            color = colors.primary,
            pendingIntent = WidgetAction.openWidgetConfigScreen(context)
        )
    }
}

private fun formattedDateTime(date: Date = Date()): String {
    val time = DateFormat.getTimeInstance(DateFormat.SHORT).format(date)
    val date = SimpleDateFormat("EE", Locale.getDefault()).format(date)
    return "$time $date"
}
