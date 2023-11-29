package com.w2sv.networking

import android.content.Context
import com.w2sv.androidutils.coroutines.getSynchronousMap
import com.w2sv.androidutils.services.getConnectivityManager
import com.w2sv.androidutils.services.getWifiManager
import com.w2sv.domain.model.IPAddress
import com.w2sv.domain.model.WidgetWifiProperty
import com.w2sv.domain.repository.WidgetRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import okhttp3.OkHttpClient
import slimber.log.i
import javax.inject.Inject

private const val COULDNT_RETRIEVE = "Couldn't retrieve"

class WidgetWifiPropertyValueViewDataFactoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val httpClient: OkHttpClient,
    private val widgetRepository: WidgetRepository
) : WidgetWifiProperty.ValueViewData.Factory {

    private val wifiManager by lazy { context.getWifiManager() }
    private val connectivityManager by lazy { context.getConnectivityManager() }

    override fun invoke(properties: Iterable<WidgetWifiProperty>): Flow<WidgetWifiProperty.ValueViewData> {
        val systemIPAddresses by lazy {
            connectivityManager.getIPAddresses().also { i { "IPAddresses: $it" } }
        }
        val ipSubPropertyEnablementMap by lazy {
            widgetRepository.getIPSubPropertyEnablementMap().getSynchronousMap()
        }

        return flow {
            properties
                .forEach { property ->
                    when (property) {
                        is WidgetWifiProperty.IP -> getIPPropertyViewData(
                            property = property,
                            systemIPAddresses = systemIPAddresses,
                            subPropertyEnablementMap = ipSubPropertyEnablementMap
                        )
                            .forEach { emit(it) }

                        is WidgetWifiProperty.NonIP -> getNonIPPropertyViewData(property)
                            .forEach { emit(it) }
                    }
                }
        }
            .flowOn(Dispatchers.IO)
    }

    private fun getNonIPPropertyViewData(
        property: WidgetWifiProperty.NonIP
    ): List<WidgetWifiProperty.ValueViewData.NonIP> =
        getPropertyViewData(
            property = property,
            values = property.getValues(),
            makeViewData = { label, value ->
                WidgetWifiProperty.ValueViewData.NonIP(value, label)
            }
        )

    @Suppress("DEPRECATION")
    private fun WidgetWifiProperty.NonIP.getValues(): List<String> =
        when (this) {
            WidgetWifiProperty.DNS -> {
                buildList {
                    add(
                        textualIPv4Representation(wifiManager.dhcpInfo.dns1)
                            ?: IPAddress.Type.V4.fallbackAddress
                    )
                    textualIPv4Representation(wifiManager.dhcpInfo.dns2)?.let {
                        add(it)
                    }
                }
            }

            else -> listOf(
                when (this) {
                    WidgetWifiProperty.SSID -> wifiManager.connectionInfo.ssid?.replace("\"", "")
                        ?: COULDNT_RETRIEVE

                    WidgetWifiProperty.BSSID -> wifiManager.connectionInfo.bssid ?: COULDNT_RETRIEVE
                    WidgetWifiProperty.Frequency -> "${wifiManager.connectionInfo.frequency} MHz"
                    WidgetWifiProperty.Channel -> frequencyToChannel(wifiManager.connectionInfo.frequency).toString()
                    WidgetWifiProperty.LinkSpeed -> "${wifiManager.connectionInfo.linkSpeed} Mbps"
                    WidgetWifiProperty.Gateway -> textualIPv4Representation(wifiManager.dhcpInfo.gateway)
                        ?: IPAddress.Type.V4.fallbackAddress

                    WidgetWifiProperty.DHCP -> textualIPv4Representation(wifiManager.dhcpInfo.serverAddress)
                        ?: IPAddress.Type.V4.fallbackAddress

                    else -> throw Error()
                }
            )
        }

    private fun getIPPropertyViewData(
        property: WidgetWifiProperty.IP,
        systemIPAddresses: List<IPAddress>,
        subPropertyEnablementMap: Map<WidgetWifiProperty.IP.SubProperty, Boolean>
    ): List<WidgetWifiProperty.ValueViewData.IPProperty> =
        getPropertyViewData(
            property = property,
            values = property
                .getAddresses(systemIPAddresses)
                .run {
                    if (property is WidgetWifiProperty.IP.V4AndV6)
                        filter {
                            subPropertyEnablementMap.getValue(
                                when (it.type) {
                                    IPAddress.Type.V4 -> property.v4EnabledSubProperty
                                    IPAddress.Type.V6 -> property.v6EnabledSubProperty
                                }
                            )
                        }
                    else
                        this
                },
            makeViewData = { label, ipAddress ->
                WidgetWifiProperty.ValueViewData.IPProperty(
                    label = label,
                    ipAddress = ipAddress,
                    showPrefixLength = subPropertyEnablementMap.getValue(property.showPrefixLengthSubProperty)
                )
            }
        )

    private fun WidgetWifiProperty.IP.getAddresses(systemIPAddresses: List<IPAddress>): List<IPAddress> =
        when (this) {
            WidgetWifiProperty.LinkLocal -> systemIPAddresses.filter { it.localAttributes.linkLocal }
            WidgetWifiProperty.SiteLocal -> systemIPAddresses.filter { it.localAttributes.siteLocal }
            WidgetWifiProperty.UniqueLocal -> systemIPAddresses.filter { it.isUniqueLocal }
            WidgetWifiProperty.GlobalUnicast -> systemIPAddresses.filter { it.isGlobalUnicast }
            WidgetWifiProperty.Public -> buildList {
                IPAddress.Type.entries.forEach {
                    getPublicIPAddress(httpClient, it)?.let { addressRepresentation ->
                        add(
                            IPAddress(
                                prefixLength = it.minPrefixLength,
                                hostAddress = addressRepresentation,
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
            }
        }

    private fun <T, R> getPropertyViewData(
        property: WidgetWifiProperty,
        values: List<T>,
        makeViewData: (String, T) -> R
    ): List<R> =
        buildList {
            val propertyLabel = context.getString(property.viewData.labelRes)

            if (values.size == 1) {
                add(makeViewData(propertyLabel, values.first()))
            } else {
                values.forEachIndexed { index, value ->
                    add(makeViewData("$propertyLabel #${index + 1}", value))
                }
            }
        }
}