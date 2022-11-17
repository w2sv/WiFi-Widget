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
            onNoWifiConnectionAvailable(context.getString(R.string.no_wifi_connection))
    } else
        onNoWifiConnectionAvailable(context.getString(R.string.wifi_disabled))
}

/**
 * dhcpInfo: 'ipaddr 192.168.1.233 gateway 192.168.1.1 netmask 0.0.0.0 dns1 192.168.1.1 dns2 0.0.0.0 DHCP server 192.168.1.1 lease 28800 seconds'
 * connectionInfo: 'connection info: SSID: , BSSID: 02:00:00:00:00:00, MAC: 02:00:00:00:00:00, Supplicant state: COMPLETED, Wi-Fi standard: 5, RSSI: -47, Link speed: 433Mbps, Tx Link speed: 433Mbps, Max Supported Tx Link speed: 433Mbps, Rx Link speed: -1Mbps, Max Supported Rx Link speed: 433Mbps, Frequency: 5180MHz, Net ID: -1, Metered hint: false, score: 60'
 */
@Suppress("DEPRECATION")
private fun RemoteViews.populatePropertiesLayout(context: Context, wifiManager: WifiManager) {
    arrayOf(
        PropertyRow(
            WidgetPreferences.showSSID,
            R.id.ssid_tv,
            R.string.ssid,
            R.id.ssid_value_tv
        ) { wifiManager.connectionInfo.ssid.replace("\"", "") },
        PropertyRow(
            WidgetPreferences.showIPv4,
            R.id.ipv4_tv,
            R.string.ipv4,
            R.id.ipv4_value_tv
        ) { wifiManager.connectionInfo.ipAddress.asFormattedIpAddress() },
        PropertyRow(
            WidgetPreferences.showFrequency,
            R.id.frequency_tv,
            R.string.frequency,
            R.id.frequency_value_tv
        ) { "${wifiManager.connectionInfo.frequency}Hz" },
        PropertyRow(
            WidgetPreferences.showGateway,
            R.id.gateway_tv,
            R.string.gateway,
            R.id.gateway_value_tv
        ) { wifiManager.dhcpInfo.gateway.asFormattedIpAddress() },
        PropertyRow(
            WidgetPreferences.showSubnetMask,
            R.id.subnet_mask_tv,
            R.string.subnet_mask,
            R.id.subnet_mask_value_tv
        ) { wifiManager.dhcpInfo.netmask.asFormattedIpAddress() },
        PropertyRow(
            WidgetPreferences.showDNS,
            R.id.dns_tv,
            R.string.dns,
            R.id.dns_value_tv
        ) { wifiManager.dhcpInfo.dns1.asFormattedIpAddress() },
        PropertyRow(
            WidgetPreferences.showDHCP,
            R.id.dhcp_tv,
            R.string.dhcp,
            R.id.dhcp_value_tv
        ) { wifiManager.dhcpInfo.serverAddress.asFormattedIpAddress() },
    )
        .forEach {
            it.setOnRemoteViews(this, context)
        }
}

private data class PropertyRow(
    val show: Boolean,
    @IdRes val propertyTextViewId: Int,
    @StringRes val propertyStringId: Int,
    @IdRes val valueTextViewId: Int,
    val getValue: () -> String
) {
    fun setOnRemoteViews(remoteViews: RemoteViews, context: Context) {
        if (show) {
            remoteViews.setTextViewText(propertyTextViewId, propertyText(context))
            remoteViews.setTextViewText(valueTextViewId, valueText(context))

            remoteViews.setViewVisibility(propertyTextViewId, View.VISIBLE)
            remoteViews.setViewVisibility(valueTextViewId, View.VISIBLE)
        } else {
            remoteViews.setViewVisibility(propertyTextViewId, View.GONE)
            remoteViews.setViewVisibility(valueTextViewId, View.GONE)
        }
    }

    private fun propertyText(context: Context): SpannableStringBuilder =
        SpannableStringBuilder()
            .italic {
                color(context.getColor(R.color.blue_chill)) {
                    append(context.getString(propertyStringId))
                }
            }

    private fun valueText(context: Context): SpannableStringBuilder =
        SpannableStringBuilder()
            .color(context.getColor(R.color.mischka_dark)){
                append(getValue())
            }
}

private fun RemoteViews.onNoWifiConnectionAvailable(statusTvText: String) {
    setTextViewText(R.id.wifi_status_tv, statusTvText)
    crossVisualize(R.id.wifi_status_tv, R.id.property_layout)
}