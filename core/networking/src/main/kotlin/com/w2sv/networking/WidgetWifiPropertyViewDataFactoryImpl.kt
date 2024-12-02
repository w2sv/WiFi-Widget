package com.w2sv.networking

import android.content.res.Resources
import android.net.ConnectivityManager
import android.net.wifi.ScanResult
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.Build
import androidx.annotation.VisibleForTesting
import com.w2sv.common.utils.SuspendingLazy
import com.w2sv.common.utils.log
import com.w2sv.core.networking.R
import com.w2sv.domain.model.WifiProperty
import com.w2sv.networking.extensions.ipAddresses
import com.w2sv.networking.extensions.linkProperties
import com.w2sv.networking.model.IFConfigData
import com.w2sv.networking.model.IPAddress
import java.net.InetAddress
import java.nio.ByteBuffer
import java.nio.ByteOrder
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import okhttp3.OkHttpClient

private typealias GetSystemIPAddresses = () -> List<IPAddress>
private typealias GetIFConfigData = suspend () -> Result<IFConfigData>

internal class WidgetWifiPropertyViewDataFactoryImpl @Inject constructor(
    private val httpClient: OkHttpClient,
    private val wifiManager: WifiManager,
    private val connectivityManager: ConnectivityManager,
    private val resources: Resources
) : WifiProperty.ViewData.Factory {

    override fun invoke(
        properties: Iterable<WifiProperty>,
        ipSubProperties: Set<WifiProperty.IP.SubProperty>
    ): Flow<WifiProperty.ViewData> {
        val systemIPAddresses by lazy {
            connectivityManager.ipAddresses().log { "Got IP Addresses" }
        }
        val ifConfigData = SuspendingLazy {
            IFConfigData.fetch(httpClient)
        }

        return flow {
            properties
                .forEach { property ->
                    property
                        .getViewData(
                            systemIPAddresses = { systemIPAddresses },
                            ipSubProperties = ipSubProperties,
                            ifConfigData = { ifConfigData.value() }
                        )
                        .forEach { emit(it) }
                }
        }
            .flowOn(Dispatchers.IO)
    }

    private suspend fun WifiProperty.getViewData(
        systemIPAddresses: GetSystemIPAddresses,
        ipSubProperties: Set<WifiProperty.IP.SubProperty>,
        ifConfigData: GetIFConfigData
    ): List<WifiProperty.ViewData> =
        when (this) {
            is WifiProperty.NonIP -> getViewData(ifConfigData)

            is WifiProperty.IP -> getViewData(
                systemIPAddresses = systemIPAddresses,
                ipSubProperties = ipSubProperties
            )
        }

    private suspend fun WifiProperty.NonIP.getViewData(ifConfigData: GetIFConfigData): List<WifiProperty.ViewData.NonIP> =
        getViewData(
            values = getValues(ifConfigData),
            resources = resources,
            makeViewData = { label, value ->
                WifiProperty.ViewData.NonIP(value, label)
            }
        )

    @Suppress("DEPRECATION")
    private suspend fun WifiProperty.NonIP.getValues(ifConfigData: GetIFConfigData): List<String> =
        buildList {
            when (this@getValues) {
                WifiProperty.NonIP.Other.DNS -> {
                    add(
                        textualIPv4Representation(wifiManager.dhcpInfo.dns1)
                            ?: IPAddress.Version.V4.fallbackAddress
                    )
                    textualIPv4Representation(wifiManager.dhcpInfo.dns2)?.let { address ->
                        if (address != IPAddress.Version.V4.fallbackAddress) {
                            add(address)
                        }
                    }
                }

                WifiProperty.NonIP.LocationAccessRequiring.SSID -> add(
                    wifiManager.connectionInfo.ssid
                        ?.replace("\"", "")
                        .takeIf { it != "<unknown ssid>" }
                        ?: resources.getString(R.string.no_location_access)
                )

                WifiProperty.NonIP.LocationAccessRequiring.BSSID -> add(
                    wifiManager.connectionInfo.bssid
                        ?.takeIf { it != "02:00:00:00:00:00" }
                        ?: resources.getString(R.string.no_location_access)
                )

                WifiProperty.NonIP.Other.Frequency -> add("${wifiManager.connectionInfo.frequency} MHz")
                WifiProperty.NonIP.Other.Channel -> add(frequencyToChannel(wifiManager.connectionInfo.frequency).toString())
                WifiProperty.NonIP.Other.LinkSpeed -> add("${wifiManager.connectionInfo.linkSpeed} Mbps")
                WifiProperty.NonIP.Other.RSSI -> add("${wifiManager.connectionInfo.rssi} dBm")
                WifiProperty.NonIP.Other.SignalStrength -> {
                    val rssi = wifiManager.connectionInfo.rssi
                    add(
                        resources.getString(
                            when {
                                rssi >= -60 -> R.string.excellent
                                rssi >= -70 -> R.string.good
                                rssi >= -80 -> R.string.fair
                                rssi >= -90 -> R.string.weak
                                else -> R.string.poor
                            }
                        )
                    )
                }

                WifiProperty.NonIP.Other.Standard -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    add(
                        when (wifiManager.connectionInfo.wifiStandard) {
                            ScanResult.WIFI_STANDARD_11AC -> "802.11ac"
                            ScanResult.WIFI_STANDARD_11AD -> "802.11ad"
                            ScanResult.WIFI_STANDARD_11AX -> "802.11ax"
                            ScanResult.WIFI_STANDARD_11BE -> "802.11be"
                            ScanResult.WIFI_STANDARD_11N -> "802.11n"
                            ScanResult.WIFI_STANDARD_LEGACY -> "802.11a/b/g"
                            else -> resources.getString(R.string.unknown)
                        }
                    )
                }

                WifiProperty.NonIP.Other.Generation -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    add(
                        when (wifiManager.connectionInfo.wifiStandard) {
                            ScanResult.WIFI_STANDARD_11AC -> "Wi-Fi 5"
                            ScanResult.WIFI_STANDARD_11AD -> "WiGig"
                            ScanResult.WIFI_STANDARD_11AX -> "Wi-Fi 6"
                            ScanResult.WIFI_STANDARD_11BE -> "Wi-Fi 7"
                            ScanResult.WIFI_STANDARD_11N -> "Wi-Fi 4"
                            ScanResult.WIFI_STANDARD_LEGACY -> "Legacy"
                            else -> resources.getString(R.string.unknown)
                        }
                    )
                }

                WifiProperty.NonIP.Other.Security -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    add(
                        when (wifiManager.connectionInfo.currentSecurityType) {
                            WifiInfo.SECURITY_TYPE_OPEN -> "Open"
                            WifiInfo.SECURITY_TYPE_WEP -> "WEP"
                            WifiInfo.SECURITY_TYPE_PSK -> "PSK (WPA/WPA2)"
                            WifiInfo.SECURITY_TYPE_EAP -> "EAP (WPA/WPA2)"
                            WifiInfo.SECURITY_TYPE_SAE -> "SAE (WPA3)"
                            WifiInfo.SECURITY_TYPE_OWE -> "OWE (WPA3)"
                            WifiInfo.SECURITY_TYPE_WAPI_PSK -> "WAPI PSK"
                            WifiInfo.SECURITY_TYPE_WAPI_CERT -> "WAPI CERT"
                            WifiInfo.SECURITY_TYPE_EAP_WPA3_ENTERPRISE -> "EAP WPA3 Enterprise"
                            WifiInfo.SECURITY_TYPE_EAP_WPA3_ENTERPRISE_192_BIT -> "EAP WPA3 Enterprise 192 Bit"
                            WifiInfo.SECURITY_TYPE_PASSPOINT_R1_R2 -> "Passpoint R1 R2"
                            WifiInfo.SECURITY_TYPE_PASSPOINT_R3 -> "Passpoint R3"
                            WifiInfo.SECURITY_TYPE_DPP -> "DPP"
                            else -> resources.getString(R.string.unknown)
                        }
                    )
                }

                WifiProperty.NonIP.Other.Gateway -> add(
                    textualIPv4Representation(wifiManager.dhcpInfo.gateway)
                        ?: IPAddress.Version.V4.fallbackAddress
                )

                WifiProperty.NonIP.Other.DHCP -> add(
                    textualIPv4Representation(wifiManager.dhcpInfo.serverAddress)
                        ?: IPAddress.Version.V4.fallbackAddress
                )

                WifiProperty.NonIP.Other.NAT64Prefix -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    add(
                        connectivityManager.linkProperties?.nat64Prefix?.address?.hostAddress
                            ?: resources.getString(R.string.none)
                    )
                }

                WifiProperty.NonIP.Other.IPLocation -> add(ifConfigData().viewDataValue { it.location })
                WifiProperty.NonIP.Other.GpsCoordinates -> add(ifConfigData().viewDataValue { it.gpsLocation })
                WifiProperty.NonIP.Other.ASN -> add(ifConfigData().viewDataValue { it.asn })
                WifiProperty.NonIP.Other.ISP -> add(ifConfigData().viewDataValue { it.asnOrg })
            }
        }

    private suspend fun WifiProperty.IP.getViewData(
        systemIPAddresses: GetSystemIPAddresses,
        ipSubProperties: Set<WifiProperty.IP.SubProperty>
    ): List<WifiProperty.ViewData.IPProperty> =
        getViewData(
            values = when (this) {
                is WifiProperty.IP.V6Only -> getAddresses(systemIPAddresses)
                is WifiProperty.IP.V4AndV6 -> getAddresses(
                    systemIPAddresses = systemIPAddresses,
                    versionsToBeIncluded = buildSet {
                        if (ipSubProperties.contains(v4EnabledSubProperty)) {
                            add(IPAddress.Version.V4)
                        }
                        if (ipSubProperties.contains(v6EnabledSubProperty)) {
                            add(IPAddress.Version.V6)
                        }
                    }
                )
            },
            resources = resources,
            makeViewData = { label, ipAddress ->
                WifiProperty.ViewData.IPProperty(
                    label = label,
                    value = ipAddress.hostAddressRepresentation,
                    subPropertyValues = buildList {
                        if (ipSubProperties.contains(showSubnetMaskSubProperty)) {
                            ipAddress.asV4OrNull?.subnetMask?.let { subnetMask ->
                                add(subnetMask)
                            }
                        }
                        if (ipSubProperties.contains(showPrefixLengthSubProperty) && ipAddress.prefixLength != null) {
                            add("/${ipAddress.prefixLength}")
                        }
                    }
                )
            }
        )

    private fun WifiProperty.IP.V6Only.getAddresses(systemIPAddresses: GetSystemIPAddresses): List<IPAddress> =
        when (this) {
            WifiProperty.IP.V6Only.ULA -> systemIPAddresses().filter { it.asV6OrNull?.isUniqueLocal == true }
            WifiProperty.IP.V6Only.GUA -> systemIPAddresses().filter { it.asV6OrNull?.isGlobalUnicast == true }
        }

    private suspend fun WifiProperty.IP.V4AndV6.getAddresses(
        systemIPAddresses: () -> List<IPAddress>,
        versionsToBeIncluded: Set<IPAddress.Version>
    ): List<IPAddress> =
        when (this) {
            WifiProperty.IP.V4AndV6.Loopback -> systemIPAddresses().filterByVersionAndPredicate(
                versionsToBeIncluded
            ) { it.isLoopback }

            WifiProperty.IP.V4AndV6.SiteLocal -> systemIPAddresses().filterByVersionAndPredicate(
                versionsToBeIncluded
            ) { it.isSiteLocal }

            WifiProperty.IP.V4AndV6.LinkLocal -> systemIPAddresses().filterByVersionAndPredicate(
                versionsToBeIncluded
            ) { it.isLinkLocal }

            WifiProperty.IP.V4AndV6.Multicast -> systemIPAddresses().filterByVersionAndPredicate(
                versionsToBeIncluded
            ) { it.isMulticast }

            WifiProperty.IP.V4AndV6.Public -> buildList {
                versionsToBeIncluded.forEach { version ->
                    IPAddress.fetchPublic(httpClient, version)
                        .onSuccess { add(it) }
                }
            }
        }
}

/**
 * @return the result of [onSuccess] or the simpleName of the held exception.
 */
private fun Result<IFConfigData>.viewDataValue(onSuccess: (IFConfigData) -> String): String =
    requireNotNull(
        getOrNull()
            ?.let(onSuccess)
            ?: exceptionOrNull()
                ?.let { it::class.simpleName }
    )

private fun <T, R : WifiProperty.ViewData> WifiProperty.getViewData(
    values: List<T>,
    resources: Resources,
    makeViewData: (String, T) -> R
): List<R> =
    buildList {
        val propertyLabel = resources.getString(
            run {
                if (this@getViewData is WifiProperty.IP) {
                    subscriptResId
                } else {
                    labelRes
                }
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

private inline fun List<IPAddress>.filterByVersionAndPredicate(
    versionsToBeIncluded: Iterable<IPAddress.Version>,
    predicate: (IPAddress) -> Boolean
): List<IPAddress> =
    filter { predicate(it) && versionsToBeIncluded.contains(it.version) }

/**
 * [Reference](https://stackoverflow.com/a/52663352/12083276)
 */
@VisibleForTesting
internal fun textualIPv4Representation(address: Int): String? =
    InetAddress
        .getByAddress(
            ByteBuffer
                .allocate(Integer.BYTES)
                .order(ByteOrder.LITTLE_ENDIAN)
                .putInt(address)
                .array()
        )
        .hostAddress

/**
 * [Reference](https://stackoverflow.com/a/58646104/12083276)
 *
 * @param frequency in MHz
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
