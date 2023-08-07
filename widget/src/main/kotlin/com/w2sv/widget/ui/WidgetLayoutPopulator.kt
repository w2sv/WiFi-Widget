package com.w2sv.widget.ui

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.view.View
import android.widget.RemoteViews
import com.w2sv.androidutils.appwidgets.crossVisualize
import com.w2sv.androidutils.appwidgets.setBackgroundColor
import com.w2sv.androidutils.appwidgets.setColorFilter
import com.w2sv.common.constants.Extra
import com.w2sv.common.utils.getAlphaSetColor
import com.w2sv.widget.PendingIntentCode
import com.w2sv.widget.R
import com.w2sv.widget.WidgetProvider
import com.w2sv.widget.WifiPropertyViewsService
import com.w2sv.widget.model.WidgetAppearance
import com.w2sv.widget.model.WidgetButtons
import com.w2sv.widget.model.WifiStatus
import com.w2sv.widget.utils.setTextView
import dagger.hilt.android.qualifiers.ApplicationContext
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

class WidgetLayoutPopulator @Inject constructor(
    private val appearance: WidgetAppearance,
    @ApplicationContext private val context: Context
) {
    private val colors by lazy {
        appearance.getColors(context)
    }

    fun populate(widget: RemoteViews, appWidgetId: Int): RemoteViews =
        widget.apply {
            setContentLayout(
                wifiStatus = WifiStatus.get(context),
                appWidgetId = appWidgetId
            )
            setBackgroundColor(
                id = R.id.widget_layout,
                color = getAlphaSetColor(colors.background, appearance.backgroundOpacity)
            )
            setLastUpdatedTV()
            setButtons(buttons = appearance.buttons)
        }

    private fun RemoteViews.setContentLayout(wifiStatus: WifiStatus, appWidgetId: Int) {
        when (wifiStatus.isConnected) {
            true -> crossVisualize(
                R.id.no_connection_available_layout, R.id.wifi_property_list_view
            )

            false -> {
                crossVisualize(
                    R.id.wifi_property_list_view, R.id.no_connection_available_layout
                )
            }
        }

        when (wifiStatus) {
            WifiStatus.Connected -> {
                setRemoteAdapter(
                    R.id.wifi_property_list_view,
                    Intent(context, WifiPropertyViewsService::class.java)
                        .apply {
                            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                            data = Uri.parse(toUri(Intent.URI_INTENT_SCHEME))
                        }
                )

                AppWidgetManager.getInstance(context)
                    .notifyAppWidgetViewDataChanged(appWidgetId, R.id.wifi_property_list_view)
            }

            else -> {
                setTextView(
                    viewId = R.id.wifi_status_tv,
                    text = context.getString(
                        if (wifiStatus == WifiStatus.Disabled)
                            com.w2sv.common.R.string.wifi_disabled
                        else
                            com.w2sv.common.R.string.no_wifi_connection
                    ),
                    color = colors.secondary
                )
            }
        }
    }

    // ============
    // Bottom Row
    // ============

    private fun RemoteViews.setLastUpdatedTV() {
        if (appearance.displayLastRefreshDateTime) {
            setViewVisibility(R.id.last_updated_tv, View.VISIBLE)
            setTextColor(R.id.last_updated_tv, colors.secondary)

            val now = Date()
            setTextViewText(
                R.id.last_updated_tv, "${
                    DateFormat.getTimeInstance(DateFormat.SHORT).format(now)
                } ${SimpleDateFormat("EE", Locale.getDefault()).format(now)}"
            )
        } else {
            setViewVisibility(R.id.last_updated_tv, View.INVISIBLE)
        }
    }

    private fun RemoteViews.setButtons(buttons: WidgetButtons) {
        setViewVisibility(
            R.id.refresh_button,
            if (buttons.refresh) View.VISIBLE else View.GONE
        )
        setViewVisibility(
            R.id.go_to_wifi_settings_button,
            if (buttons.goToWifiSettings) View.VISIBLE else View.GONE
        )
        setViewVisibility(
            R.id.go_to_widget_settings_button,
            if (buttons.goToWidgetSettings) View.VISIBLE else View.GONE
        )

        setColorFilter(R.id.refresh_button, colors.primary)
        setColorFilter(R.id.go_to_widget_settings_button, colors.primary)
        setColorFilter(R.id.go_to_wifi_settings_button, colors.primary)

        setOnClickPendingIntent(
            R.id.refresh_button,
            PendingIntent.getBroadcast(
                context,
                PendingIntentCode.RefreshWidgetData.ordinal,
                WidgetProvider.getRefreshDataIntent(context),
                PendingIntent.FLAG_IMMUTABLE
            )
        )

        setOnClickPendingIntent(
            R.id.go_to_wifi_settings_button,
            PendingIntent.getActivity(
                context,
                PendingIntentCode.GoToWifiSettings.ordinal,
                Intent(Settings.ACTION_WIFI_SETTINGS)
                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK),
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
        )

        setOnClickPendingIntent(
            R.id.go_to_widget_settings_button,
            PendingIntent.getActivity(
                context,
                PendingIntentCode.LaunchHomeActivity.ordinal,
                Intent.makeRestartActivityTask(
                    ComponentName(
                        context, "com.w2sv.wifiwidget.ui.MainActivity"
                    )
                ).putExtra(
                    Extra.OPEN_WIDGET_CONFIGURATION_DIALOG, true
                ),
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
        )
    }
}