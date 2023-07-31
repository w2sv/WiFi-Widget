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
import androidx.annotation.ColorInt
import androidx.core.graphics.ColorUtils
import com.w2sv.androidutils.appwidgets.crossVisualize
import com.w2sv.androidutils.appwidgets.setBackgroundColor
import com.w2sv.androidutils.appwidgets.setColorFilter
import com.w2sv.androidutils.coroutines.getSynchronousMap
import com.w2sv.androidutils.coroutines.getValueSynchronously
import com.w2sv.common.connectivityManager
import com.w2sv.common.constants.Extra
import com.w2sv.common.data.sources.Theme
import com.w2sv.common.data.sources.WidgetColor
import com.w2sv.common.data.sources.WidgetRefreshingParameter
import com.w2sv.common.data.storage.WidgetConfigurationRepository
import com.w2sv.common.extensions.isNightModeActiveCompat
import com.w2sv.common.extensions.toRGBChannelInt
import com.w2sv.common.isWifiConnected
import com.w2sv.common.linkProperties
import com.w2sv.common.wifiManager
import com.w2sv.widget.PendingIntentCode
import com.w2sv.widget.R
import com.w2sv.widget.WidgetProvider
import com.w2sv.widget.WifiPropertyViewsService
import com.w2sv.widget.ui.model.WifiStatus
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import slimber.log.i
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

class WidgetLayoutPopulator @Inject constructor(
    @ApplicationContext private val context: Context,
    private val widgetConfigurationRepository: WidgetConfigurationRepository
) {

    fun populate(widget: RemoteViews, appWidgetId: Int): RemoteViews =
        widget.apply {
            setContentLayout(
                wifiStatus = when (context.wifiManager.isWifiEnabled) {
                    false -> WifiStatus.Disabled
                    true -> {
                        when (context.connectivityManager.isWifiConnected) {
                            true, null -> WifiStatus.Connected
                                .also {
                                    @Suppress("DEPRECATION")
                                    (i {
                                        "wifiManager.connectionInfo: ${context.wifiManager.connectionInfo}\n" +
                                                "wifiManager.dhcpInfo: ${context.wifiManager.dhcpInfo}\n" +
                                                "connectivityManager.linkProperties: ${context.connectivityManager.linkProperties}"
                                    })
                                }

                            false -> WifiStatus.Disconnected
                        }
                    }
                },
                appWidgetId = appWidgetId
            )
            setWidgetColors(
                theme = widgetConfigurationRepository.theme.getValueSynchronously(),
                customWidgetColors = widgetConfigurationRepository.customColors,
                backgroundOpacity = widgetConfigurationRepository.opacity.getValueSynchronously(),
                context = context
            )
            setLastUpdatedTV()
            setOnClickPendingIntents()
        }

    private fun RemoteViews.setContentLayout(wifiStatus: WifiStatus, appWidgetId: Int) {
        when (wifiStatus) {
            WifiStatus.Connected -> {
                setLayout(true)

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

            WifiStatus.Disabled -> {
                setLayout(false)
                setTextViewText(
                    R.id.wifi_status_tv,
                    context.getString(com.w2sv.common.R.string.wifi_disabled)
                )
            }

            WifiStatus.Disconnected -> {
                setLayout(false)
                setTextViewText(
                    R.id.wifi_status_tv,
                    context.getString(com.w2sv.common.R.string.no_wifi_connection)
                )
            }
        }
    }

    private fun RemoteViews.setLayout(wifiConnected: Boolean) {
        when (wifiConnected) {
            true -> crossVisualize(
                R.id.no_connection_available_layout,
                R.id.wifi_property_list_view
            )

            false -> {
                crossVisualize(
                    R.id.wifi_property_list_view,
                    R.id.no_connection_available_layout
                )

                setOnClickPendingIntent(
                    R.id.no_connection_available_layout,
                    PendingIntent.getActivity(
                        context,
                        PendingIntentCode.LaunchHomeActivity.ordinal,
                        Intent(Settings.ACTION_WIFI_SETTINGS)
                            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK),
                        PendingIntent.FLAG_IMMUTABLE
                    )
                )
            }
        }
    }

    // ============
    // Bottom Row
    // ============

    private fun RemoteViews.setLastUpdatedTV() {
        when (widgetConfigurationRepository.refreshingParameters.getValue(WidgetRefreshingParameter.ShowDateTime)
            .getValueSynchronously()) {
            true -> {
                setViewVisibility(R.id.last_updated_tv, View.VISIBLE)

                val now = Date()
                setTextViewText(
                    R.id.last_updated_tv,
                    "${
                        DateFormat.getTimeInstance(DateFormat.SHORT).format(now)
                    } ${SimpleDateFormat("EE", Locale.getDefault()).format(now)}"
                )
            }

            false -> {
                setViewVisibility(R.id.last_updated_tv, View.INVISIBLE)
            }
        }
    }

    private fun RemoteViews.setOnClickPendingIntents() {
        // refresh_button
        setOnClickPendingIntent(
            R.id.refresh_button,
            PendingIntent.getBroadcast(
                context,
                PendingIntentCode.RefreshWidgetData.ordinal,
                WidgetProvider.getRefreshDataIntent(context),
                PendingIntent.FLAG_IMMUTABLE
            )
        )

        // settings_button
        setOnClickPendingIntent(
            R.id.settings_button,
            PendingIntent.getActivity(
                context,
                PendingIntentCode.LaunchHomeActivity.ordinal,
                Intent.makeRestartActivityTask(
                    ComponentName(
                        context,
                        "com.w2sv.wifiwidget.ui.MainActivity"
                    )
                )
                    .putExtra(
                        Extra.OPEN_WIDGET_CONFIGURATION_DIALOG,
                        true
                    ),
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
        )
    }
}

private fun RemoteViews.setWidgetColors(
    theme: Theme,
    customWidgetColors: Map<WidgetColor, Flow<Int>>,
    backgroundOpacity: Float,
    context: Context
) {
    when (theme) {
        Theme.Dark -> setColors(
            context.getColor(R.color.background_dark),
            backgroundOpacity,
            context.getColor(R.color.foreground_dark)
        )

        Theme.DeviceDefault -> {
            when (context.resources.configuration.isNightModeActiveCompat) {
                false -> setWidgetColors(
                    Theme.Light,
                    customWidgetColors,
                    backgroundOpacity,
                    context
                )

                true -> setWidgetColors(
                    Theme.Dark,
                    customWidgetColors,
                    backgroundOpacity,
                    context
                )
            }
        }

        Theme.Light -> setColors(
            context.getColor(R.color.background_light),
            backgroundOpacity,
            context.getColor(R.color.background_light)
        )

        Theme.Custom -> with(customWidgetColors.getSynchronousMap()) {
            setColors(
                getValue(WidgetColor.Background),
                backgroundOpacity,
                getValue(WidgetColor.Other)
            )
        }
    }
}

private fun RemoteViews.setColors(
    @ColorInt background: Int,
    backgroundOpacity: Float,
    @ColorInt foreground: Int
) {
    // Background
    setBackgroundColor(
        R.id.widget_layout,
        ColorUtils.setAlphaComponent(
            background,
            backgroundOpacity.toRGBChannelInt()
        )
    )

    // TVs
    setTextColor(R.id.wifi_status_tv, foreground)
    setTextColor(R.id.last_updated_tv, foreground)

    // ImageButtons
    setColorFilter(R.id.settings_button, foreground)
    setColorFilter(R.id.refresh_button, foreground)
}
