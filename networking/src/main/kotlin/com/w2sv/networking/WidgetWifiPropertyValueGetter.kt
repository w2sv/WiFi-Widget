package com.w2sv.networking

import android.content.Context
import com.w2sv.androidutils.services.getConnectivityManager
import com.w2sv.androidutils.services.getWifiManager
import com.w2sv.domain.model.IPAddress
import com.w2sv.domain.model.WidgetWifiProperty
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.OkHttpClient
import javax.inject.Inject

class WidgetWifiPropertyValueGetter @Inject constructor(
    @ApplicationContext private val context: Context,
    private val httpClient: OkHttpClient
) : WidgetWifiProperty.ValueGetter {

    private val wifiManager by lazy { context.getWifiManager() }
    private val connectivityManager by lazy { context.getConnectivityManager() }

    override fun invoke(properties: List<WidgetWifiProperty>): List<WidgetWifiProperty.Value> {
        val systemIPAddresses by lazy { connectivityManager.getIPAddresses() }

        return properties.map { getPropertyValue(it, systemIPAddresses) }
    }

    @Suppress("DEPRECATION")
    private fun getPropertyValue(
        property: WidgetWifiProperty,
        systemIPAddresses: List<IPAddress>
    ): WidgetWifiProperty.Value =
        when (property) {
            WidgetWifiProperty.SSID -> {
                WidgetWifiProperty.Value.String(wifiManager.connectionInfo.ssid?.replace("\"", ""))
            }

            WidgetWifiProperty.BSSID -> {
                WidgetWifiProperty.Value.String(wifiManager.connectionInfo.bssid)
            }

            WidgetWifiProperty.LinkLocal -> {
                WidgetWifiProperty.Value.IPAddresses(systemIPAddresses.filter { it.localAttributes.linkLocal })
            }

            WidgetWifiProperty.SiteLocal -> {
                WidgetWifiProperty.Value.IPAddresses(systemIPAddresses.filter { it.localAttributes.siteLocal })
            }

            WidgetWifiProperty.UniqueLocal -> {
                WidgetWifiProperty.Value.IPAddresses(systemIPAddresses.filter { it.isUniqueLocal })
            }

            WidgetWifiProperty.GlobalUnicast -> {
                WidgetWifiProperty.Value.IPAddresses(systemIPAddresses.filter { it.isGlobalUnicast })
            }

            WidgetWifiProperty.Public -> {
                WidgetWifiProperty.Value.IPAddresses(
                    buildList {
                        getPublicIPAddress(httpClient)?.let {
                            add(
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
                        }
                    }
                )
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
}