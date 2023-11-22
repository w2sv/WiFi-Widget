package com.w2sv.networking

import android.content.Context
import android.net.ConnectivityManager
import android.net.wifi.WifiManager
import com.w2sv.androidutils.services.getConnectivityManager
import com.w2sv.androidutils.services.getWifiManager
import com.w2sv.domain.model.IPAddress
import com.w2sv.domain.model.WidgetWifiProperty
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class WidgetWifiPropertyValueGetter(
    private val wifiManager: WifiManager,
    private val systemIPAddresses: List<IPAddress>
) {

    constructor(wifiManager: WifiManager, connectivityManager: ConnectivityManager) : this(
        wifiManager = wifiManager,
        systemIPAddresses = connectivityManager.getIPAddresses(),
    )

    class Provider @Inject constructor(@ApplicationContext private val context: Context) {

        private val wifiManager by lazy { context.getWifiManager() }
        private val connectivityManager by lazy { context.getConnectivityManager() }

        fun provide(): WidgetWifiPropertyValueGetter =
            WidgetWifiPropertyValueGetter(wifiManager, connectivityManager)
    }

    @Suppress("DEPRECATION")
    operator fun invoke(property: WidgetWifiProperty): WidgetWifiProperty.Value? =
        when (property) {
            WidgetWifiProperty.SSID -> {
                wifiManager.connectionInfo.ssid?.replace("\"", "")
                    ?.let { WidgetWifiProperty.Value.String(it) }
            }

            WidgetWifiProperty.BSSID -> {
                wifiManager.connectionInfo.bssid?.let { WidgetWifiProperty.Value.String(it) }
            }

            WidgetWifiProperty.LinkLocal -> {
                getIPPropertyValue { it.localAttributes.linkLocal }
            }

            WidgetWifiProperty.SiteLocal -> {
                getIPPropertyValue { it.localAttributes.siteLocal }
            }

            WidgetWifiProperty.UniqueLocal -> {
                getIPPropertyValue { it.isUniqueLocal }
            }

            WidgetWifiProperty.GlobalUnicast -> {
                getIPPropertyValue { it.isGlobalUnicast }
            }

            WidgetWifiProperty.Public -> {

            }

            WidgetWifiProperty.Frequency -> {
                WidgetWifiProperty.Value.String("${wifiManager.connectionInfo.frequency} MHz")
            }

            WidgetWifiProperty.Channel -> {
                WidgetWifiProperty.Value.String(frequencyToChannel(wifiManager.connectionInfo.frequency).toString())
            }

            WidgetWifiProperty.LinkSpeed -> {
                WidgetWifiProperty.Value.String("${wifiManager.connectionInfo.linkSpeed} Mbps")
            }

            WidgetWifiProperty.Gateway -> {
                WidgetWifiProperty.Value.String(
                    textualIPv4Representation(wifiManager.dhcpInfo.gateway)
                        ?: IPAddress.Type.V4.fallbackAddress,
                )
            }

            WidgetWifiProperty.DNS -> {
                WidgetWifiProperty.Value.String(
                    textualIPv4Representation(wifiManager.dhcpInfo.dns1)
                        ?: IPAddress.Type.V4.fallbackAddress,
                )
            }

            WidgetWifiProperty.DHCP -> {
                WidgetWifiProperty.Value.String(
                    textualIPv4Representation(wifiManager.dhcpInfo.serverAddress)
                        ?: IPAddress.Type.V4.fallbackAddress,
                )
            }
        }

    private fun getIPPropertyValue(systemIPAddressesFilter: (IPAddress) -> Boolean): WidgetWifiProperty.Value.IPAddresses? =
        systemIPAddresses
            .filter(systemIPAddressesFilter)
            .run {
                if (isEmpty())
                    null
                else
                    WidgetWifiProperty.Value.IPAddresses(this)
            }
}