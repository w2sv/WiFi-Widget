package com.w2sv.widget

import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.wifi.WifiManager
import android.provider.Settings
import android.view.View
import android.widget.RemoteViews
import androidx.annotation.IdRes
import androidx.annotation.StringRes
import com.w2sv.androidutils.extensions.crossVisualize
import com.w2sv.common.preferences.CustomWidgetColors
import com.w2sv.common.preferences.EnumOrdinals
import com.w2sv.common.preferences.FloatPreferences
import com.w2sv.common.preferences.WidgetProperties
import com.w2sv.kotlinutils.extensions.getByOrdinal
import com.w2sv.widget.utils.asFormattedIpAddress
import com.w2sv.widget.utils.isWifiConnected
import com.w2sv.widget.utils.netmask
import com.w2sv.widget.utils.setMakeUniqueActivityFlags
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

internal class WidgetLayoutPopulator @Inject constructor(
    @ApplicationContext private val context: Context,
    private val widgetProperties: WidgetProperties,
    private val enumOrdinals: EnumOrdinals,
    private val floatPreferences: FloatPreferences,
    private val customWidgetColors: CustomWidgetColors
) {

    @InstallIn(SingletonComponent::class)
    @EntryPoint
    interface EntryPointInterface {
        fun getInstance(): WidgetLayoutPopulator
    }

    companion object {
        fun getInstance(context: Context): WidgetLayoutPopulator =
            EntryPointAccessors.fromApplication(
                context,
                EntryPointInterface::class.java
            )
                .getInstance()
    }

    fun populate(remoteViews: RemoteViews): RemoteViews =
        remoteViews.apply {
            setWidgetColors(
                getByOrdinal(enumOrdinals.widgetTheme),
                customWidgetColors,
                floatPreferences.opacity,
                context
            )
            setConnectionDependentLayout()
            setConnectionIndependentLayout()
        }

    private fun RemoteViews.setConnectionDependentLayout() {
        when (context.getSystemService(WifiManager::class.java).isWifiEnabled) {
            true -> {
                when (context.getSystemService(ConnectivityManager::class.java).isWifiConnected) {
                    true -> {
                        populatePropertiesLayout()
                        setWidgetSettingsButton(connectionAvailable = true)
                        setLayout(connectionAvailable = true)
                    }

                    false -> onNoWifiConnectionAvailable(context.getString(R.string.no_wifi_connection))
                }
            }

            false -> onNoWifiConnectionAvailable(context.getString(R.string.wifi_disabled))
        }
    }

    private fun RemoteViews.setConnectionIndependentLayout() {
        setLastUpdatedTV()

        // refresh_button OnClickPendingIntent
        setOnClickPendingIntent(
            R.id.refresh_button,
            WifiWidgetProvider.getRefreshDataPendingIntent(context)
        )

        // connection_dependent_layout OnClickPendingIntent
        setOnClickPendingIntent(
            R.id.widget_layout,
            PendingIntent.getActivity(
                context,
                PendingIntentCode.LaunchHomeActivity.ordinal,
                Intent(Settings.ACTION_WIFI_SETTINGS)
                    .setMakeUniqueActivityFlags(),
                PendingIntent.FLAG_IMMUTABLE
            )
        )
    }

    @Suppress("DEPRECATION")
    private fun RemoteViews.populatePropertiesLayout() {
        val wifiManager = context.getSystemService(WifiManager::class.java)

        setWifiPropertyRow(
            R.id.ssid_row,
            R.string.ssid,
            R.id.ssid_value_tv
        ) { wifiManager.connectionInfo.ssid.replace("\"", "") }
        setWifiPropertyRow(
            R.id.ip_row,
            R.string.ip,
            R.id.ip_value_tv
        ) { wifiManager.connectionInfo.ipAddress.asFormattedIpAddress() }
        setWifiPropertyRow(
            R.id.frequency_row,
            R.string.frequency,
            R.id.frequency_value_tv
        ) { "${wifiManager.connectionInfo.frequency} MHz" }
        setWifiPropertyRow(
            R.id.linkspeed_row,
            R.string.linkspeed,
            R.id.linkspeed_value_tv
        ) { "${wifiManager.connectionInfo.linkSpeed} Mbps" }
        setWifiPropertyRow(
            R.id.gateway_row,
            R.string.gateway,
            R.id.gateway_value_tv
        ) { wifiManager.dhcpInfo.gateway.asFormattedIpAddress() }
        setWifiPropertyRow(
            R.id.dns_row,
            R.string.dns,
            R.id.dns_value_tv
        ) { wifiManager.dhcpInfo.dns1.asFormattedIpAddress() }
        setWifiPropertyRow(
            R.id.dhcp_row,
            R.string.dhcp,
            R.id.dhcp_value_tv
        ) { wifiManager.dhcpInfo.serverAddress.asFormattedIpAddress() }
        setWifiPropertyRow(
            R.id.netmask_row,
            R.string.netmask,
            R.id.netmask_value_tv
        ) { netmask() }
    }

    private fun RemoteViews.setWifiPropertyRow(
        @IdRes layout: Int,
        @StringRes property: Int,
        @IdRes valueTV: Int,
        getValue: () -> String
    ) {
        when (widgetProperties[context.getString(property)]!!) {
            true -> {
                setTextViewText(valueTV, getValue())
                setViewVisibility(layout, View.VISIBLE)
            }

            else -> setViewVisibility(layout, View.GONE)
        }
    }

    private fun RemoteViews.setWidgetSettingsButton(connectionAvailable: Boolean) {
        when (connectionAvailable) {
            true -> {
                setViewVisibility(R.id.settings_button, View.VISIBLE)
                setOnClickPendingIntent(
                    R.id.settings_button,
                    PendingIntent.getActivity(
                        context,
                        PendingIntentCode.LaunchHomeActivity.ordinal,
                        Intent.makeRestartActivityTask(
                            ComponentName(
                                context,
                                "com.w2sv.wifiwidget.activities.HomeActivity"
                            )
                        )
                            .putExtra(
                                WifiWidgetProvider.EXTRA_OPEN_CONFIGURATION_DIALOG_ON_START,
                                true
                            ),
                        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                    )
                )
            }

            false -> setViewVisibility(R.id.settings_button, View.GONE)
        }
    }

    private fun RemoteViews.onNoWifiConnectionAvailable(statusText: String) {
        setTextViewText(R.id.wifi_status_tv, statusText)
        setWidgetSettingsButton(false)
        setLayout(false)
    }

    private fun RemoteViews.setLayout(connectionAvailable: Boolean) {
        when (connectionAvailable) {
            false -> crossVisualize(
                R.id.wifi_properties_layout,
                R.id.no_connection_available_layout
            )

            true -> crossVisualize(R.id.no_connection_available_layout, R.id.wifi_properties_layout)
        }
    }
}

private fun RemoteViews.setLastUpdatedTV() {
    val now = Date()
    setTextViewText(
        R.id.last_updated_tv,
        "${
            DateFormat.getTimeInstance(DateFormat.SHORT).format(now)
        } ${SimpleDateFormat("EE", Locale.getDefault()).format(now)}"
    )
}