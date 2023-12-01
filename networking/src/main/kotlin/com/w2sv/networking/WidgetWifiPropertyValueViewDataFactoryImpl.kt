package com.w2sv.networking

import android.content.res.Resources
import android.net.ConnectivityManager
import android.net.wifi.WifiManager
import com.w2sv.androidutils.coroutines.getSynchronousMap
import com.w2sv.domain.model.WidgetWifiProperty
import com.w2sv.domain.repository.WidgetRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import okhttp3.OkHttpClient
import okhttp3.Request
import slimber.log.i
import java.io.IOException
import java.net.InetAddress
import java.nio.ByteBuffer
import java.nio.ByteOrder
import javax.inject.Inject

private const val COULDNT_RETRIEVE = "Couldn't retrieve"

class WidgetWifiPropertyValueViewDataFactoryImpl @Inject constructor(
    private val httpClient: OkHttpClient,
    private val wifiManager: WifiManager,
    private val connectivityManager: ConnectivityManager,
    private val widgetRepository: WidgetRepository,
    private val resources: Resources
) : WidgetWifiProperty.ValueViewData.Factory {

    override fun invoke(properties: Iterable<WidgetWifiProperty>): Flow<WidgetWifiProperty.ValueViewData> {
        val systemIPAddresses by lazy {
            connectivityManager.getIPAddresses()
//                .also {
//                    it.forEachIndexed { index, ipAddress ->
//                        i { "IPAddress #${index + 1}: $ipAddress" }
//                    }
//                }
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
                    textualIPv4Representation(wifiManager.dhcpInfo.dns2)?.let { address ->
                        if (address != IPAddress.Type.V4.fallbackAddress) {
                            add(address)
                        }
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
                    value = ipAddress.hostAddressRepresentation,
                    prefixLengthText = if (subPropertyEnablementMap[property.showPrefixLengthSubProperty] == true) "/${ipAddress.prefixLength}" else null
                )
            }
        )

    private fun WidgetWifiProperty.IP.getAddresses(systemIPAddresses: List<IPAddress>): List<IPAddress> =
        when (this) {
            WidgetWifiProperty.LinkLocal -> systemIPAddresses.filter { it.isLinkLocal }
            WidgetWifiProperty.SiteLocal -> systemIPAddresses.filter { it.isSiteLocal }
            WidgetWifiProperty.UniqueLocal -> systemIPAddresses.filter { it.isUniqueLocal }
            WidgetWifiProperty.GlobalUnicast -> systemIPAddresses.filter { it.isGlobalUnicast }
            WidgetWifiProperty.Public -> buildList {
                IPAddress.Type.entries.forEach { type ->
                    getPublicIPAddress(httpClient, type)?.let { addressRepresentation ->
                        if (type.ofCorrectFormat(addressRepresentation)) {
                            add(
                                IPAddress(
                                    prefixLength = type.minPrefixLength,
                                    hostAddress = addressRepresentation,
                                    isLinkLocal = false,
                                    isSiteLocal = false,
                                    isAnyLocal = false,
                                    isLoopback = false,
                                    isMulticast = false,
                                    isMCGlobal = false,
                                    isMCLinkLocal = false,
                                    isMCSiteLocal = false,
                                    isMCNodeLocal = false,
                                    isMCOrgLocal = false
                                )
                            )
                        }
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
            val propertyLabel = resources.getString(property.viewData.labelRes)

            if (values.size == 1) {
                add(makeViewData(propertyLabel, values.first()))
            } else {
                values.forEachIndexed { index, value ->
                    add(makeViewData("$propertyLabel ${index + 1}", value))
                }
            }
        }
}

/**
 * Reference: https://stackoverflow.com/a/52663352/12083276
 */
private fun textualIPv4Representation(address: Int): String? =
    InetAddress.getByAddress(
        ByteBuffer
            .allocate(Integer.BYTES)
            .order(ByteOrder.LITTLE_ENDIAN)
            .putInt(address)
            .array(),
    )
        .hostAddress

private fun getPublicIPAddress(httpClient: OkHttpClient, type: IPAddress.Type): String? {
    i { "Getting public address for $type" }

    val request = Request.Builder()
        .url(
            when (type) {
                IPAddress.Type.V4 -> "https://api.ipify.org"
                IPAddress.Type.V6 -> "https://api64.ipify.org"
            }
        )
        .build()

    try {
        return httpClient
            .newCall(request)
            .execute()
            .body
            ?.string()
            .also { i { "Got public address for $type" } }
    } catch (_: IOException) {
        i { "getPublicIPAddress.exception" }
    }

    return null
}

/**
 * Reference: https://stackoverflow.com/a/58646104/12083276
 *
 * @param frequency in MHz.
 */
private fun frequencyToChannel(frequency: Int): Int =
    when {
        frequency <= 0 -> -1
        frequency == 2484 -> 14
        frequency < 2484 -> (frequency - 2407) / 5
        frequency in 4910..4980 -> (frequency - 4000) / 5
        frequency < 5925 -> (frequency - 5000) / 5
        frequency == 5935 -> 2
        frequency <= 45000 -> (frequency - 5950) / 5
        frequency in 58320..70200 -> (frequency - 56160) / 2160
        else -> -1
    }