package com.w2sv.networking

import android.content.res.Resources
import android.net.ConnectivityManager
import android.net.wifi.WifiManager
import com.w2sv.androidutils.coroutines.getSynchronousMap
import com.w2sv.domain.model.WidgetWifiProperty
import com.w2sv.domain.repository.WidgetRepository
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
                WidgetWifiProperty.DNS -> {
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

                WidgetWifiProperty.SSID -> add(
                    wifiManager.connectionInfo.ssid?.replace("\"", "")
                        ?: resources.getString(R.string.couldnt_retrieve)
                )

                WidgetWifiProperty.BSSID -> add(
                    wifiManager.connectionInfo.bssid
                        ?: resources.getString(R.string.couldnt_retrieve)
                )

                WidgetWifiProperty.Frequency -> add("${wifiManager.connectionInfo.frequency} MHz")
                WidgetWifiProperty.Channel -> add(frequencyToChannel(wifiManager.connectionInfo.frequency).toString())
                WidgetWifiProperty.LinkSpeed -> add("${wifiManager.connectionInfo.linkSpeed} Mbps")
                WidgetWifiProperty.Gateway -> add(
                    textualIPv4Representation(wifiManager.dhcpInfo.gateway)
                        ?: IPAddress.Type.V4.fallbackAddress
                )

                WidgetWifiProperty.DHCP -> add(
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
            WidgetWifiProperty.LinkLocal -> systemIPAddresses.filter { it.isLinkLocal }
            WidgetWifiProperty.SiteLocal -> systemIPAddresses.filter { it.isSiteLocal }
            WidgetWifiProperty.UniqueLocal -> systemIPAddresses.filter { it.isUniqueLocal }
            WidgetWifiProperty.GlobalUnicast -> systemIPAddresses.filter { it.isGlobalUnicast }
            WidgetWifiProperty.Public -> buildList {
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

private suspend fun getPublicIPAddress(httpClient: OkHttpClient, type: IPAddress.Type): String? {
    i { "Getting public address for $type" }

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
                        address.also { i { "Got public address for $type" } }
                    else
                        null.also { i { "Discarded $address obtained for $type" } }
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