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
import com.w2sv.wifiwidget.widget.utils.asFormattedIpAddress
import com.w2sv.wifiwidget.widget.utils.crossVisualize
import com.w2sv.wifiwidget.widget.utils.isWifiConnected
import com.w2sv.wifiwidget.widget.utils.netmask

/**
 * connectivityManager.getLinkProperties(connectivityManager.activeNetwork)!! -> {InterfaceName: wlan0 LinkAddresses: [ fe80::ac57:89ff:fe22:9f70/64,192.168.1.233/24,2a02:3036:20a:9df2:ac57:89ff:fe22:9f70/64,2a02:3036:20a:9df2:79d9:9c65:ad9e:81ab/64 ] DnsAddresses: [ /192.168.1.1 ] Domains: null MTU: 1500 ServerAddress: /192.168.1.1 TcpBufferSizes: 1730560,3461120,6922240,524288,1048576,4525824 Routes: [ fe80::/64 -> :: wlan0 mtu 0,::/0 -> fe80::49c8:81bb:cfd2:ce7a wlan0 mtu 0,2a02:3036:20a:9df2::/64 -> :: wlan0 mtu 0,192.168.1.0/24 -> 0.0.0.0 wlan0 mtu 0,0.0.0.0/0 -> 192.168.1.1 wlan0 mtu 0 ]}
 */
fun RemoteViews.setWifiDependentContent(context: Context, widgetPreferences: WidgetPreferences) {
    val wifiManager = context.getSystemService(WifiManager::class.java)

    when(wifiManager.isWifiEnabled){
        true -> {
            when(context.getSystemService(ConnectivityManager::class.java).isWifiConnected){
                true -> {
                    populatePropertiesLayout(context, wifiManager, widgetPreferences)
                    crossVisualize(R.id.property_layout, R.id.wifi_status_tv)
                }
                false -> onNoWifiConnectionAvailable(context.getString(R.string.no_wifi_connection))
            }
        }
        false -> onNoWifiConnectionAvailable(context.getString(R.string.wifi_disabled))
    }
}

/**
 * dhcpInfo: 'ipaddr 192.168.1.233 gateway 192.168.1.1 netmask 0.0.0.0 dns1 192.168.1.1 dns2 0.0.0.0 DHCP server 192.168.1.1 lease 28800 seconds'
 * connectionInfo: 'connection info: SSID: , BSSID: 02:00:00:00:00:00, MAC: 02:00:00:00:00:00, Supplicant state: COMPLETED, Wi-Fi standard: 5, RSSI: -47, Link speed: 433Mbps, Tx Link speed: 433Mbps, Max Supported Tx Link speed: 433Mbps, Rx Link speed: -1Mbps, Max Supported Rx Link speed: 433Mbps, Frequency: 5180MHz, Net ID: -1, Metered hint: false, score: 60'
 */
@Suppress("DEPRECATION")
private fun RemoteViews.populatePropertiesLayout(context: Context, wifiManager: WifiManager, widgetPreferences: WidgetPreferences) {
    arrayOf(
        PropertyRow(
            widgetPreferences.showSSID,
            R.id.ssid_tv,
            R.string.ssid,
            R.id.ssid_value_tv
        ) { wifiManager.connectionInfo.ssid.replace("\"", "") },
        PropertyRow(
            widgetPreferences.showIPv4,
            R.id.ipv4_tv,
            R.string.ipv4,
            R.id.ipv4_value_tv
        ) { wifiManager.connectionInfo.ipAddress.asFormattedIpAddress() },
        PropertyRow(
            widgetPreferences.showFrequency,
            R.id.frequency_tv,
            R.string.frequency,
            R.id.frequency_value_tv
        ) { "${wifiManager.connectionInfo.frequency}Hz" },
        PropertyRow(
            widgetPreferences.showGateway,
            R.id.gateway_tv,
            R.string.gateway,
            R.id.gateway_value_tv
        ) { wifiManager.dhcpInfo.gateway.asFormattedIpAddress() },
        PropertyRow(
            widgetPreferences.showSubnetMask,
            R.id.subnet_mask_tv,
            R.string.netmask,
            R.id.subnet_mask_value_tv
        ) { netmask() },
        PropertyRow(
            widgetPreferences.showDNS,
            R.id.dns_tv,
            R.string.dns,
            R.id.dns_value_tv
        ) { wifiManager.dhcpInfo.dns1.asFormattedIpAddress() },
        PropertyRow(
            widgetPreferences.showDHCP,
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
            .color(context.getColor(R.color.mischka_dark)) {
                append(getValue())
            }
}

private fun RemoteViews.onNoWifiConnectionAvailable(statusTvText: String) {
    setTextViewText(R.id.wifi_status_tv, statusTvText)
    crossVisualize(R.id.wifi_status_tv, R.id.property_layout)
}