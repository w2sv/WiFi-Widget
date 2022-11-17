package com.w2sv.wifiwidget.widget

import android.content.Context
import android.net.ConnectivityManager
import android.net.wifi.WifiManager
import android.text.SpannableStringBuilder
import android.view.View
import android.widget.RemoteViews
import androidx.annotation.IdRes
import androidx.annotation.StringRes
import androidx.core.text.color
import androidx.core.text.italic
import com.w2sv.wifiwidget.R
import com.w2sv.wifiwidget.preferences.WidgetPreferences

fun RemoteViews.setWifiDependentContent(context: Context) {
    val wifiManager = context.getSystemService(WifiManager::class.java)

    if (wifiManager.isWifiEnabled) {
        if (context.getSystemService(ConnectivityManager::class.java).isWifiConnected) {
            populatePropertiesLayout(context, wifiManager)
            crossVisualize(R.id.property_layout, R.id.wifi_status_tv)
        } else
            onNoWifiConnection(context.getString(R.string.no_wifi_connection))
    } else
        onNoWifiConnection(context.getString(R.string.wifi_disabled))
}

/**
 * dhcpInfo: 'ipaddr 192.168.1.233 gateway 192.168.1.1 netmask 0.0.0.0 dns1 192.168.1.1 dns2 0.0.0.0 DHCP server 192.168.1.1 lease 28800 seconds'
 * connectionInfo: 'connection info: SSID: , BSSID: 02:00:00:00:00:00, MAC: 02:00:00:00:00:00, Supplicant state: COMPLETED, Wi-Fi standard: 5, RSSI: -47, Link speed: 433Mbps, Tx Link speed: 433Mbps, Max Supported Tx Link speed: 433Mbps, Rx Link speed: -1Mbps, Max Supported Rx Link speed: 433Mbps, Frequency: 5180MHz, Net ID: -1, Metered hint: false, score: 60'
 */
@Suppress("DEPRECATION")
private fun RemoteViews.populatePropertiesLayout(context: Context, wifiManager: WifiManager) {
    arrayOf(
        PropertyRow(
            R.id.ssid_tv,
            R.string.ssid,
            WidgetPreferences.showSSID
        ) { wifiManager.connectionInfo.ssid.replace("\"", "") },
        PropertyRow(
            R.id.ipv4_tv,
            R.string.ipv4,
            WidgetPreferences.showIPv4
        ) { wifiManager.connectionInfo.ipAddress.asFormattedIpAddress() },
        PropertyRow(
            R.id.frequency_tv,
            R.string.frequency,
            WidgetPreferences.showFrequency
        ) { "${wifiManager.connectionInfo.frequency}Hz" },
        PropertyRow(
            R.id.gateway_tv,
            R.string.gateway,
            WidgetPreferences.showGateway
        ) { wifiManager.dhcpInfo.gateway.asFormattedIpAddress() },
        PropertyRow(
            R.id.subnet_mask_tv,
            R.string.subnet_mask,
            WidgetPreferences.showSubnetMask
        ) { wifiManager.dhcpInfo.netmask.asFormattedIpAddress() },
        PropertyRow(
            R.id.dns_tv,
            R.string.dns,
            WidgetPreferences.showDNS
        ) { wifiManager.dhcpInfo.dns1.asFormattedIpAddress() },
        PropertyRow(
            R.id.dhcp_tv,
            R.string.dhcp,
            WidgetPreferences.showDHCP
        ) { wifiManager.dhcpInfo.serverAddress.asFormattedIpAddress() },
    )
        .forEach {
            if (it.show) {
                setTextViewText(it.textViewId, it.render(context))
                setViewVisibility(it.textViewId, View.VISIBLE)
            } else {
                setViewVisibility(it.textViewId, View.GONE)
            }
        }
}

private data class PropertyRow(
    @IdRes val textViewId: Int,
    @StringRes val stringId: Int,
    val show: Boolean,
    val getValue: () -> String
) {
    fun render(context: Context): SpannableStringBuilder =
        SpannableStringBuilder()
            .italic {
                color(context.getColor(R.color.blue_chill_dark)) {
                    append("${context.getString(stringId)} ")
                }
            }
            .append(getValue())
}

private fun RemoteViews.onNoWifiConnection(statusTvText: String) {
    setTextViewText(R.id.wifi_status_tv, statusTvText)
    crossVisualize(R.id.wifi_status_tv, R.id.property_layout)
}