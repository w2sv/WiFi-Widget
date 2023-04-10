package com.w2sv.widget

import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.wifi.WifiManager
import android.provider.Settings
import android.widget.RemoteViews
import com.w2sv.androidutils.extensions.crossVisualize
import com.w2sv.common.preferences.CustomWidgetColors
import com.w2sv.common.preferences.DataStoreRepository
import com.w2sv.common.preferences.WifiProperties
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
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
    private val wifiProperties: WifiProperties,
    private val dataStoreRepository: DataStoreRepository,
    private val customWidgetColors: CustomWidgetColors
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

    fun populate(widget: RemoteViews): RemoteViews =
        widget.apply {
            setContentLayout(
                wifiStatus = when (context.getSystemService(WifiManager::class.java).isWifiEnabled) {
                    false -> WifiStatus.Disabled
                    true -> {
                        when (context.getSystemService(ConnectivityManager::class.java).isWifiConnected) {
                            true -> WifiStatus.Connected
                            false -> WifiStatus.Disconnected
                        }
                    }
                }
            )
            setWidgetColors(
                theme = runBlocking { dataStoreRepository.widgetTheme.first() },
                customWidgetColors = customWidgetColors,
                backgroundOpacity = runBlocking { dataStoreRepository.opacity.first() },
                context = context
            )
            setLastUpdatedTV()
            setOnClickPendingIntents()
        }

    private fun RemoteViews.setContentLayout(wifiStatus: WifiStatus) {
        when (wifiStatus) {
            WifiStatus.Connected -> {
                setLayout(true)
                setWifiProperties(context, wifiProperties)
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
                R.id.wifi_properties_layout
            )

            false -> crossVisualize(
                R.id.wifi_properties_layout,
                R.id.no_connection_available_layout
            )
        }
    }

    // ============
    // Bottom Row
    // ============

    private fun RemoteViews.setLastUpdatedTV() {
        val now = Date()
        setTextViewText(
            R.id.last_updated_tv,
            "${
                DateFormat.getTimeInstance(DateFormat.SHORT).format(now)
            } ${SimpleDateFormat("EE", Locale.getDefault()).format(now)}"
        )
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
                        "com.w2sv.wifiwidget.ui.screens.home.HomeActivity"
                    )
                )
                    .putExtra(
                        WidgetProvider.EXTRA_OPEN_CONFIGURATION_DIALOG_ON_START,
                        true
                    ),
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
        )

        // widget_layout
        setOnClickPendingIntent(
            R.id.widget_layout,
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