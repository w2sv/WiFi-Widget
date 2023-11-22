package com.w2sv.networking

import android.content.Context
import com.w2sv.androidutils.services.getConnectivityManager
import com.w2sv.androidutils.services.getWifiManager
import com.w2sv.domain.model.IPAddress
import com.w2sv.domain.model.WidgetWifiProperty
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.OkHttpClient

class WidgetWifiPropertyValueGetter(
    @ApplicationContext private val context: Context,
    private val httpClient: OkHttpClient
) : WidgetWifiProperty.ValueGetter {

    private val wifiManager by lazy { context.getWifiManager() }
    private val connectivityManager by lazy { context.getConnectivityManager() }

    override fun invoke(properties: List<WidgetWifiProperty>): List<WidgetWifiProperty.Value?> {
        val systemIPAddresses by lazy { connectivityManager.getIPAddresses() }

        return properties.map { getPropertyValue(it, systemIPAddresses) }
    }

    @Suppress("DEPRECATION")
    private fun getPropertyValue(
        property: WidgetWifiProperty,
        systemIPAddresses: List<IPAddress>
    ): WidgetWifiProperty.Value? =
        when (property) {
            WidgetWifiProperty.SSID -> {
                wifiManager.connectionInfo.ssid?.replace("\"", "")
                    ?.let { WidgetWifiProperty.Value.String(it) }
            }

            WidgetWifiProperty.BSSID -> {
                wifiManager.connectionInfo.bssid?.let { WidgetWifiProperty.Value.String(it) }
            }

            WidgetWifiProperty.LinkLocal -> {
                systemIPAddresses.filter { it.localAttributes.linkLocal }.asValueOrNull()
            }

            WidgetWifiProperty.SiteLocal -> {
                systemIPAddresses.filter { it.localAttributes.siteLocal }.asValueOrNull()
            }

            WidgetWifiProperty.UniqueLocal -> {
                systemIPAddresses.filter { it.isUniqueLocal }.asValueOrNull()
            }

            WidgetWifiProperty.GlobalUnicast -> {
                systemIPAddresses.filter { it.isGlobalUnicast }.asValueOrNull()
            }

            WidgetWifiProperty.Public -> {
                getPublicIPAddress(httpClient)?.let {
                    WidgetWifiProperty.Value.IPAddresses(
                        listOf(
                            IPAddress(
                                prefixLength = 0,
                                hostAddress = it,
                                localAttributes = IPAddress.LocalAttributes(
                                    linkLocal = false,
                                    siteLocal = false,
                                    anyLocal = false
                                ),
                                isLoopback = false,
                                isMulticast = false
                            )
                        )
                    )
                }
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

    private fun List<IPAddress>.asValueOrNull(): WidgetWifiProperty.Value.IPAddresses? =
        if (isEmpty())
            null
        else
            WidgetWifiProperty.Value.IPAddresses(this)
}