package com.w2sv.networking

import android.content.Context
import com.w2sv.androidutils.services.getConnectivityManager
import com.w2sv.androidutils.services.getWifiManager
import com.w2sv.domain.model.IPAddress
import com.w2sv.domain.model.WidgetWifiProperty
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.OkHttpClient
import javax.inject.Inject

class WidgetWifiPropertyValueViewDataFactoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val httpClient: OkHttpClient
) : WidgetWifiProperty.ValueViewData.Factory {

    private val wifiManager by lazy { context.getWifiManager() }
    private val connectivityManager by lazy { context.getConnectivityManager() }

    override fun invoke(properties: Iterable<WidgetWifiProperty>): List<WidgetWifiProperty.ValueViewData> {
        val systemIPAddresses by lazy { connectivityManager.getIPAddresses() }

        return properties.flatMap {
            when (it) {
                is WidgetWifiProperty.IPProperty -> getIPPropertyViewData(it, systemIPAddresses)
                else -> listOf(getRegularPropertyViewData(it))
            }
        }
    }

    @Suppress("DEPRECATION")
    private fun getRegularPropertyViewData(
        property: WidgetWifiProperty
    ): WidgetWifiProperty.ValueViewData.RegularProperty =
        when (property) {
            WidgetWifiProperty.SSID -> {
                getRegularPropertyViewData(
                    property = property,
                    value = wifiManager.connectionInfo.ssid?.replace("\"", "")
                )
            }

            WidgetWifiProperty.BSSID -> {
                getRegularPropertyViewData(
                    property = property,
                    value = wifiManager.connectionInfo.bssid
                )
            }

            WidgetWifiProperty.Frequency -> {
                getRegularPropertyViewData(property, "${wifiManager.connectionInfo.frequency} MHz")
            }

            WidgetWifiProperty.Channel -> {
                getRegularPropertyViewData(
                    property,
                    frequencyToChannel(wifiManager.connectionInfo.frequency).toString()
                )
            }

            WidgetWifiProperty.LinkSpeed -> {
                getRegularPropertyViewData(property, "${wifiManager.connectionInfo.linkSpeed} Mbps")
            }

            WidgetWifiProperty.Gateway -> {
                getRegularPropertyViewData(
                    property,
                    textualIPv4Representation(wifiManager.dhcpInfo.gateway)
                        ?: IPAddress.Type.V4.fallbackAddress
                )
            }

            WidgetWifiProperty.DNS -> {
                getRegularPropertyViewData(
                    property,
                    textualIPv4Representation(wifiManager.dhcpInfo.dns1)  // TODO: include dns2 if present
                        ?: IPAddress.Type.V4.fallbackAddress
                )
            }

            WidgetWifiProperty.DHCP -> {
                getRegularPropertyViewData(
                    property,
                    textualIPv4Representation(wifiManager.dhcpInfo.serverAddress)
                        ?: IPAddress.Type.V4.fallbackAddress
                )
            }

            else -> throw Exception()  // TODO
        }

    private fun getRegularPropertyViewData(
        property: WidgetWifiProperty,
        value: String?
    ): WidgetWifiProperty.ValueViewData.RegularProperty =
        WidgetWifiProperty.ValueViewData.RegularProperty(
            label = context.getString(property.viewData.labelRes),
            value = value ?: "Couldn't retrieve"
        )

    private fun getIPPropertyViewData(
        property: WidgetWifiProperty.IPProperty,
        systemIPAddresses: List<IPAddress>
    ): List<WidgetWifiProperty.ValueViewData.IPProperty> =
        when (property) {
            WidgetWifiProperty.LinkLocal -> {
                getIPPropertyViewData(
                    property,
                    systemIPAddresses.filter { it.localAttributes.linkLocal },
                    true
                )
            }

            WidgetWifiProperty.SiteLocal -> {
                getIPPropertyViewData(
                    property,
                    systemIPAddresses.filter { it.localAttributes.siteLocal },
                    true
                )
            }

            WidgetWifiProperty.UniqueLocal -> {
                getIPPropertyViewData(property, systemIPAddresses.filter { it.isUniqueLocal }, true)
            }

            WidgetWifiProperty.GlobalUnicast -> {
                getIPPropertyViewData(
                    property,
                    systemIPAddresses.filter { it.isGlobalUnicast },
                    true
                )
            }

            WidgetWifiProperty.Public -> {
                buildList {
                    getPublicIPAddress(httpClient)?.let { addressRepresentation ->
                        add(
                            WidgetWifiProperty.ValueViewData.IPProperty(
                                label = context.getString(property.viewData.labelRes),
                                value = addressRepresentation,
                                prefixLengthText = null
                            )
                        )
                    }
                }
            }
        }

    private fun getIPPropertyViewData(
        property: WidgetWifiProperty.IPProperty,
        ipAddresses: List<IPAddress>,
        showPrefixLength: Boolean
    ): List<WidgetWifiProperty.ValueViewData.IPProperty> =
        if (ipAddresses.size == 1) {
            listOf(
                WidgetWifiProperty.ValueViewData.IPProperty(
                    label = context.getString(property.viewData.labelRes),
                    ipAddress = ipAddresses.first(),
                    showPrefixLength = showPrefixLength
                )
            )
        } else {
            ipAddresses.mapIndexed { index, ipAddress ->
                WidgetWifiProperty.ValueViewData.IPProperty(
                    label = "${context.getString(property.viewData.labelRes)} #${index + 1}",
                    ipAddress = ipAddress,
                    showPrefixLength = showPrefixLength
                )
            }
        }
}