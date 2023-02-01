package com.w2sv.wifiwidget.widget

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.wifi.WifiManager
import android.view.View
import android.widget.RemoteViews
import androidx.annotation.IdRes
import androidx.annotation.StringRes
import com.w2sv.wifiwidget.R
import com.w2sv.wifiwidget.activities.HomeActivity
import com.w2sv.wifiwidget.preferences.WidgetProperties
import com.w2sv.wifiwidget.utils.setMakeUniqueActivityFlags
import com.w2sv.wifiwidget.widget.utils.asFormattedIpAddress
import com.w2sv.wifiwidget.widget.extensions.crossVisualize
import com.w2sv.wifiwidget.widget.utils.isWifiConnected
import com.w2sv.wifiwidget.widget.utils.netmask
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Inject

internal class ConnectionDependentWidgetLayoutSetter @Inject constructor() {

    @InstallIn(SingletonComponent::class)
    @EntryPoint
    interface EntryPointInterface {
        fun getInstance(): ConnectionDependentWidgetLayoutSetter
    }

    companion object {
        fun getInstance(context: Context): ConnectionDependentWidgetLayoutSetter =
            EntryPointAccessors.fromApplication(
                context,
                EntryPointInterface::class.java
            )
                .getInstance()
    }

    @Inject
    @ApplicationContext
    lateinit var context: Context

    @Inject
    lateinit var widgetProperties: WidgetProperties

    /**
     * connectivityManager.getLinkProperties(connectivityManager.activeNetwork)!! -> {InterfaceName: wlan0 LinkAddresses: [ fe80::ac57:89ff:fe22:9f70/64,192.168.1.233/24,2a02:3036:20a:9df2:ac57:89ff:fe22:9f70/64,2a02:3036:20a:9df2:79d9:9c65:ad9e:81ab/64 ] DnsAddresses: [ /192.168.1.1 ] Domains: null MTU: 1500 ServerAddress: /192.168.1.1 TcpBufferSizes: 1730560,3461120,6922240,524288,1048576,4525824 Routes: [ fe80::/64 -> :: wlan0 mtu 0,::/0 -> fe80::49c8:81bb:cfd2:ce7a wlan0 mtu 0,2a02:3036:20a:9df2::/64 -> :: wlan0 mtu 0,192.168.1.0/24 -> 0.0.0.0 wlan0 mtu 0,0.0.0.0/0 -> 192.168.1.1 wlan0 mtu 0 ]}
     */
    fun populate(remoteViews: RemoteViews) {
        with(remoteViews) {
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
        ) { "${wifiManager.connectionInfo.frequency}Hz" }
        setWifiPropertyRow(
            R.id.gateway_row,
            R.string.gateway,
            R.id.gateway_tv,
            R.id.gateway_value_tv
        ) { wifiManager.dhcpInfo.gateway.asFormattedIpAddress() }
        setWifiPropertyRow(
            R.id.netmask_row,
            R.string.netmask,
            R.id.netmask_tv,
            R.id.netmask_value_tv
        ) { netmask() }
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

    private fun RemoteViews.setWidgetSettingsButton(connectionAvailable: Boolean){
        when(connectionAvailable){
            true -> {
                setViewVisibility(R.id.settings_button, View.VISIBLE)
                setOnClickPendingIntent(
                    R.id.settings_button,
                    PendingIntent.getActivity(
                        context,
                        PendingIntentCode.LaunchHomeActivity.ordinal,
                        Intent(context, HomeActivity::class.java)
                            .setMakeUniqueActivityFlags()
                            .putExtra(
                                HomeActivity.EXTRA_OPEN_PROPERTIES_CONFIGURATION_DIALOG_ON_START,
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

    private fun RemoteViews.setLayout(connectionAvailable: Boolean){
        when(connectionAvailable){
            false -> crossVisualize(R.id.no_connection_available_layout, R.id.wifi_properties_layout)
            true -> crossVisualize(R.id.wifi_properties_layout, R.id.no_connection_available_layout)
        }
    }
}