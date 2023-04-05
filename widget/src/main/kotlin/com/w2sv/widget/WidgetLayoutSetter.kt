package com.w2sv.widget

import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.net.ConnectivityManager
import android.net.wifi.WifiManager
import android.provider.Settings
import android.view.View
import android.widget.RemoteViews
import androidx.annotation.ColorInt
import androidx.annotation.IdRes
import androidx.annotation.StringRes
import androidx.core.graphics.ColorUtils
import com.w2sv.androidutils.extensions.crossVisualize
import com.w2sv.common.Theme
import com.w2sv.common.extensions.toRGBInt
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

internal class WidgetLayoutSetter @Inject constructor(
    @ApplicationContext private val context: Context,
    private val widgetProperties: WidgetProperties,
    private val enumOrdinals: EnumOrdinals,
    private val floatPreferences: FloatPreferences,
    private val customWidgetColors: CustomWidgetColors
) {

    @InstallIn(SingletonComponent::class)
    @EntryPoint
    interface EntryPointInterface {
        fun getInstance(): WidgetLayoutSetter
    }

    companion object {
        fun getInstance(context: Context): WidgetLayoutSetter =
            EntryPointAccessors.fromApplication(
                context,
                EntryPointInterface::class.java
            )
                .getInstance()
    }

    /**
     * connectivityManager.getLinkProperties(connectivityManager.activeNetwork)!! -> {InterfaceName: wlan0 LinkAddresses: [ fe80::ac57:89ff:fe22:9f70/64,192.168.1.233/24,2a02:3036:20a:9df2:ac57:89ff:fe22:9f70/64,2a02:3036:20a:9df2:79d9:9c65:ad9e:81ab/64 ] DnsAddresses: [ /192.168.1.1 ] Domains: null MTU: 1500 ServerAddress: /192.168.1.1 TcpBufferSizes: 1730560,3461120,6922240,524288,1048576,4525824 Routes: [ fe80::/64 -> :: wlan0 mtu 0,::/0 -> fe80::49c8:81bb:cfd2:ce7a wlan0 mtu 0,2a02:3036:20a:9df2::/64 -> :: wlan0 mtu 0,192.168.1.0/24 -> 0.0.0.0 wlan0 mtu 0,0.0.0.0/0 -> 192.168.1.1 wlan0 mtu 0 ]}
     */
    fun populated(remoteViews: RemoteViews): RemoteViews =
        remoteViews.apply {
            setColors(getByOrdinal(enumOrdinals.widgetTheme))
            setConnectionDependentLayout()
            setConnectionIndependentLayout()
        }

    private fun RemoteViews.setColors(theme: Theme) {
        when (theme) {
            Theme.Dark -> setColors(
                context.getColor(android.R.color.background_dark),
                context.getColor(com.w2sv.common.R.color.blue_chill),
                context.getColor(androidx.appcompat.R.color.foreground_material_dark)
            )

            Theme.Light -> setColors(
                context.getColor(android.R.color.background_light),
                context.getColor(com.w2sv.common.R.color.blue_chill),
                context.getColor(androidx.appcompat.R.color.foreground_material_light)
            )

            Theme.DeviceDefault -> {
                when (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
                    Configuration.UI_MODE_NIGHT_NO -> setColors(Theme.Light)
                    Configuration.UI_MODE_NIGHT_YES -> setColors(Theme.Dark)
                }
            }

            Theme.Custom -> setColors(
                customWidgetColors["Background"]!!,
                customWidgetColors["Labels"]!!,
                customWidgetColors["Other"]!!
            )
        }
    }

    private fun RemoteViews.setColors(
        @ColorInt background: Int,
        @ColorInt labels: Int,
        @ColorInt other: Int
    ) {
        // Background
        setInt(
            R.id.widget_layout,
            "setBackgroundColor",
            ColorUtils.setAlphaComponent(
                background,
                floatPreferences.opacity.toRGBInt()
            )
        )

        listOf(
            R.id.ssid_tv,
            R.id.ip_tv,
            R.id.frequency_tv,
            R.id.linkspeed_tv,
            R.id.gateway_tv,
            R.id.dhcp_tv,
            R.id.dns_tv,
            R.id.netmask_tv
        )
            .forEach {
                setInt(it, "setTextColor", labels)
            }

        // 'Other' TVs
        listOf(
            R.id.ssid_value_tv,
            R.id.ip_value_tv,
            R.id.frequency_value_tv,
            R.id.linkspeed_value_tv,
            R.id.gateway_value_tv,
            R.id.dhcp_value_tv,
            R.id.dns_value_tv,
            R.id.netmask_value_tv,

            R.id.wifi_status_tv,
            R.id.go_to_wifi_settings_tv,
            R.id.last_updated_tv
        )
            .forEach {
                setInt(it, "setTextColor", other)
            }

        // 'Other' ImageButtons
        listOf(
            R.id.settings_button,
            R.id.refresh_button
        )
            .forEach {
                setInt(it, "setColorFilter", other)
            }
    }

    private fun RemoteViews.setConnectionDependentLayout() {
        when (context.getSystemService(WifiManager::class.java).isWifiEnabled) {
            true -> {
                when (context.getSystemService(ConnectivityManager::class.java).isWifiConnected) {
                    true -> {
                        populatePropertiesLayout()
                        setWidgetSettingsButton(true)
                        setLayout(true)
                    }

                    false -> onNoWifiConnectionAvailable(context.getString(R.string.no_wifi_connection))
                }
            }

            false -> onNoWifiConnectionAvailable(context.getString(R.string.wifi_disabled))
        }
    }

    private fun RemoteViews.setConnectionIndependentLayout() {
        // set last_updated_tv text
        setLastUpdatedTV()

        /**
         * OnClickPendingIntents
         */

        // refresh_button
        setOnClickPendingIntent(
            R.id.refresh_button,
            WifiWidgetProvider.getRefreshDataPendingIntent(context)
        )

        // connection_dependent_layout
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

    /**
     * dhcpInfo: 'ipaddr 192.168.1.233 gateway 192.168.1.1 netmask 0.0.0.0 dns1 192.168.1.1 dns2 0.0.0.0 DHCP server 192.168.1.1 lease 28800 seconds'
     * connectionInfo: 'connection info: SSID: , BSSID: 02:00:00:00:00:00, MAC: 02:00:00:00:00:00, Supplicant state: COMPLETED, Wi-Fi standard: 5, RSSI: -47, Link speed: 433Mbps, Tx Link speed: 433Mbps, Max Supported Tx Link speed: 433Mbps, Rx Link speed: -1Mbps, Max Supported Rx Link speed: 433Mbps, Frequency: 5180MHz, Net ID: -1, Metered hint: false, score: 60'
     */
    @Suppress("DEPRECATION")
    private fun RemoteViews.populatePropertiesLayout() {
        val wifiManager = context.getSystemService(WifiManager::class.java)

        setWifiPropertyRow(
            R.id.ssid_row,
            R.string.ssid,
            R.id.ssid_tv,
            R.id.ssid_value_tv
        ) { wifiManager.connectionInfo.ssid.replace("\"", "") }
        setWifiPropertyRow(
            R.id.ip_row,
            R.string.ip,
            R.id.ip_tv,
            R.id.ip_value_tv
        ) { wifiManager.connectionInfo.ipAddress.asFormattedIpAddress() }
        setWifiPropertyRow(
            R.id.frequency_row,
            R.string.frequency,
            R.id.frequency_tv,
            R.id.frequency_value_tv
        ) { "${wifiManager.connectionInfo.frequency} MHz" }
        setWifiPropertyRow(
            R.id.linkspeed_row,
            R.string.linkspeed,
            R.id.linkspeed_tv,
            R.id.linkspeed_value_tv
        ) { "${wifiManager.connectionInfo.linkSpeed} Mbps" }
        setWifiPropertyRow(
            R.id.gateway_row,
            R.string.gateway,
            R.id.gateway_tv,
            R.id.gateway_value_tv
        ) { wifiManager.dhcpInfo.gateway.asFormattedIpAddress() }
        setWifiPropertyRow(
            R.id.dns_row,
            R.string.dns,
            R.id.dns_tv,
            R.id.dns_value_tv
        ) { wifiManager.dhcpInfo.dns1.asFormattedIpAddress() }
        setWifiPropertyRow(
            R.id.dhcp_row,
            R.string.dhcp,
            R.id.dhcp_tv,
            R.id.dhcp_value_tv
        ) { wifiManager.dhcpInfo.serverAddress.asFormattedIpAddress() }
        setWifiPropertyRow(
            R.id.netmask_row,
            R.string.netmask,
            R.id.netmask_tv,
            R.id.netmask_value_tv
        ) { netmask() }
    }

    private fun RemoteViews.setWifiPropertyRow(
        @IdRes layout: Int,
        @StringRes property: Int,
        @IdRes labelTV: Int,
        @IdRes valueTV: Int,
        getValue: () -> String
    ) {
        if (widgetProperties.getValue(context.getString(property))) {
            setTextViewText(labelTV, context.getString(property))
            setTextViewText(valueTV, getValue())

            setViewVisibility(layout, View.VISIBLE)
        } else {
            setViewVisibility(layout, View.GONE)
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