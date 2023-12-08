package com.w2sv.networking

import android.content.res.Resources
import android.net.ConnectivityManager
import android.net.wifi.WifiManager
import com.w2sv.domain.model.WidgetWifiProperty
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withTimeout
import okhttp3.OkHttpClient
import okhttp3.Request
import slimber.log.i
import java.net.InetAddress
import java.nio.ByteBuffer
import java.nio.ByteOrder
import javax.inject.Inject

class WidgetWifiPropertyValueViewDataFactoryImpl @Inject constructor(
    private val httpClient: OkHttpClient,
    private val wifiManager: WifiManager,
    private val connectivityManager: ConnectivityManager,
    private val resources: Resources
) : WidgetWifiProperty.ValueViewData.Factory {

    override fun invoke(
        properties: Iterable<WidgetWifiProperty>,
        ipSubPropertyEnablementMap: Map<WidgetWifiProperty.IP.SubProperty, Boolean>
    ): Flow<WidgetWifiProperty.ValueViewData> {

        val systemIPAddresses by lazy {
            connectivityManager.getIPAddresses()
        }

        return flow {
            properties
                .forEach { property ->
                    property
                        .getPropertyViewData(systemIPAddresses, ipSubPropertyEnablementMap)
                        .forEach { emit(it) }
                }
        }
            .flowOn(Dispatchers.IO)
    }

    private suspend fun WidgetWifiProperty.getPropertyViewData(
        systemIPAddresses: List<IPAddress>,
        subPropertyEnablementMap: Map<WidgetWifiProperty.IP.SubProperty, Boolean>
    ): List<WidgetWifiProperty.ValueViewData> =
        when (this) {
            is WidgetWifiProperty.NonIP -> getNonIPPropertyViewData(this)

            is WidgetWifiProperty.IP -> getIPPropertyViewData(
                property = this,
                systemIPAddresses = systemIPAddresses,
                subPropertyEnablementMap = subPropertyEnablementMap
            )
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
        buildList {
            when (this@getValues) {
                WidgetWifiProperty.NonIP.Other.DNS -> {
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

                WidgetWifiProperty.NonIP.LocationAccessRequiring.SSID -> add(
                    wifiManager.connectionInfo.ssid
                        ?.replace("\"", "")
                        .takeIf { it != "<unknown ssid>" }
                        ?: resources.getString(R.string.no_location_access)
                )

                WidgetWifiProperty.NonIP.LocationAccessRequiring.BSSID -> add(
                    wifiManager.connectionInfo.bssid
                        ?.takeIf { it != "02:00:00:00:00:00" }
                        ?: resources.getString(R.string.no_location_access)
                )

                WidgetWifiProperty.NonIP.Other.Frequency -> add("${wifiManager.connectionInfo.frequency} MHz")
                WidgetWifiProperty.NonIP.Other.Channel -> add(frequencyToChannel(wifiManager.connectionInfo.frequency).toString())
                WidgetWifiProperty.NonIP.Other.LinkSpeed -> add("${wifiManager.connectionInfo.linkSpeed} Mbps")
                WidgetWifiProperty.NonIP.Other.Gateway -> add(
                    textualIPv4Representation(wifiManager.dhcpInfo.gateway)
                        ?: IPAddress.Type.V4.fallbackAddress
                )

                WidgetWifiProperty.NonIP.Other.DHCP -> add(
                    textualIPv4Representation(wifiManager.dhcpInfo.serverAddress)
                        ?: IPAddress.Type.V4.fallbackAddress
                )
            }
        }

    private suspend fun getIPPropertyViewData(
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

    private suspend fun WidgetWifiProperty.IP.getAddresses(systemIPAddresses: List<IPAddress>): List<IPAddress> =
        when (this) {
            WidgetWifiProperty.IP.V4AndV6.Loopback -> systemIPAddresses.filter { it.isLoopback }
            WidgetWifiProperty.IP.V4AndV6.SiteLocal -> systemIPAddresses.filter { it.isSiteLocal }
            WidgetWifiProperty.IP.V4AndV6.LinkLocal -> systemIPAddresses.filter { it.isLinkLocal }
            WidgetWifiProperty.IP.V6Only.ULA -> systemIPAddresses.filter { it.isUniqueLocal }
            WidgetWifiProperty.IP.V4AndV6.Multicast -> systemIPAddresses.filter { it.isMulticast }
            WidgetWifiProperty.IP.V6Only.GUA -> systemIPAddresses.filter { it.isGlobalUnicast }
            WidgetWifiProperty.IP.V4AndV6.Public -> buildList {
                IPAddress.Type.entries.forEach { type ->
                    getPublicIPAddress(httpClient, type)?.let { addressRepresentation ->
                        add(
                            IPAddress(
                                prefixLength = type.minPrefixLength,
                                hostAddress = addressRepresentation,
                                isLinkLocal = false,
                                isSiteLocal = false,
                                isAnyLocal = false,
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
            val propertyLabel = resources.getString(
                property.run {
                    if (this is WidgetWifiProperty.IP)
                        subscriptResId
                    else
                        labelRes
                }
            )

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

private suspend fun getPublicIPAddress(httpClient: OkHttpClient, type: IPAddress.Type): String? {
    i { "Getting public $type address" }

    val request = Request.Builder()
        .url(
            when (type) {
                IPAddress.Type.V4 -> "https://api.ipify.org"
                IPAddress.Type.V6 -> "https://api64.ipify.org"
            }
        )
        .build()

    return try {
        withTimeout(5_000) {
            httpClient
                .newCall(request)
                .execute()
                .body
                ?.string()
                ?.let { address ->
                    if (type.ofCorrectFormat(address))
                        address.also { i { "Got public $type address" } }
                    else
                        null.also { i { "Discarded obtained $type address $address due to incorrect format" } }
                }
        }
    } catch (e: Exception) {
        i {
            if (e is TimeoutCancellationException) {
                "Timed out trying to get public $type address"
            } else {
                "Caught $e"
            }
        }
        null
    }
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