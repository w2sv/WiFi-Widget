package com.w2sv.widget

import android.content.Context
import android.net.wifi.WifiManager
import android.view.View
import android.widget.RemoteViews
import androidx.annotation.IdRes
import com.w2sv.common.WifiProperty
import com.w2sv.common.preferences.WidgetProperties

@Suppress("DEPRECATION")
internal fun RemoteViews.setWifiProperties(context: Context, widgetProperties: WidgetProperties) {
    val wifiManager = context.getSystemService(WifiManager::class.java)

    setPropertyRow(
        widgetProperties.get(WifiProperty.SSID),
        R.id.ssid_row,
        R.id.ssid_value_tv
    ) { wifiManager.connectionInfo.ssid.replace("\"", "") }
    setPropertyRow(
        widgetProperties.get(WifiProperty.IP),
        R.id.ip_row,
        R.id.ip_value_tv
    ) { wifiManager.connectionInfo.ipAddress.asFormattedIpAddress() }
    setPropertyRow(
        widgetProperties.get(WifiProperty.Frequency),
        R.id.frequency_row,
        R.id.frequency_value_tv
    ) { "${wifiManager.connectionInfo.frequency} MHz" }
    setPropertyRow(
        widgetProperties.get(WifiProperty.Linkspeed),
        R.id.linkspeed_row,
        R.id.linkspeed_value_tv
    ) { "${wifiManager.connectionInfo.linkSpeed} Mbps" }
    setPropertyRow(
        widgetProperties.get(WifiProperty.Gateway),
        R.id.gateway_row,
        R.id.gateway_value_tv
    ) { wifiManager.dhcpInfo.gateway.asFormattedIpAddress() }
    setPropertyRow(
        widgetProperties.get(WifiProperty.DNS),
        R.id.dns_row,
        R.id.dns_value_tv
    ) { wifiManager.dhcpInfo.dns1.asFormattedIpAddress() }
    setPropertyRow(
        widgetProperties.get(WifiProperty.DHCP),
        R.id.dhcp_row,
        R.id.dhcp_value_tv
    ) { wifiManager.dhcpInfo.serverAddress.asFormattedIpAddress() }
    setPropertyRow(
        widgetProperties.get(WifiProperty.Netmask),
        R.id.netmask_row,
        R.id.netmask_value_tv
    ) { getNetmask() }
}

private fun RemoteViews.setPropertyRow(
    display: Boolean,
    @IdRes layout: Int,
    @IdRes valueTV: Int,
    getValue: () -> String
) {
    when (display) {
        true -> setTextViewText(valueTV, getValue())
        else -> setViewVisibility(layout, View.GONE)
    }
}