package com.w2sv.widget

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
import com.w2sv.androidutils.coroutines.getValueSynchronously
import com.w2sv.common.connectivityManager
import com.w2sv.common.data.repositories.WidgetConfigurationRepository
import com.w2sv.common.enums.WidgetRefreshingParameter
import com.w2sv.common.isWifiConnected
import com.w2sv.common.linkProperties
import com.w2sv.common.wifiManager
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import slimber.log.i
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

private enum class WifiStatus {
    Disabled,
    Disconnected,
    Connected
}

internal class WidgetPopulator @Inject constructor(
    @ApplicationContext private val context: Context,
    private val widgetConfigurationRepository: WidgetConfigurationRepository
) {

    @InstallIn(SingletonComponent::class)
    @EntryPoint
    interface EntryPointInterface {
        fun getWidgetPopulatorInstance(): WidgetPopulator
    }

    companion object {
        fun getWidgetPopulatorInstance(context: Context): WidgetPopulator =
            EntryPointAccessors.fromApplication(
                context,
                EntryPointInterface::class.java
            )
                .getWidgetPopulatorInstance()
    }

    // ============
    // Populating
    // ============

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
                                    i {
                                        "wifiManager.connectionInfo: ${context.wifiManager.connectionInfo}\n" +
                                                "wifiManager.dhcpInfo: ${context.wifiManager.dhcpInfo}\n" +
                                                "connectivityManager.linkProperties: ${context.connectivityManager.linkProperties}"
                                    }
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
                    Intent(context, WifiPropertiesService::class.java)
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
        when(widgetConfigurationRepository.refreshingParameters.getValue(WidgetRefreshingParameter.ShowDateTime).getValueSynchronously()){
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
            WidgetProvider.getRefreshDataPendingIntent(context)
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
                        WidgetProvider.EXTRA_OPEN_CONFIGURATION_DIALOG_ON_START,
                        true
                    ),
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
        )
    }
}