package com.w2sv.networking

import android.content.res.Resources
import android.net.ConnectivityManager
import android.net.wifi.ScanResult
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.Build
import androidx.annotation.VisibleForTesting
import com.w2sv.core.networking.R
import com.w2sv.domain.model.IpAddress
import com.w2sv.domain.model.IpApiData
import com.w2sv.domain.model.RemoteNetworkInfo
import com.w2sv.domain.model.WifiProperty
import com.w2sv.domain.model.WifiViewData
import com.w2sv.networking.extensions.linkProperties
import java.net.InetAddress
import java.nio.ByteBuffer
import java.nio.ByteOrder
import javax.inject.Inject

internal class WifiViewDataProviderImpl @Inject constructor(
    private val wifiManager: WifiManager,
    private val connectivityManager: ConnectivityManager,
    private val resources: Resources
) : WifiViewData.Provider {

    override fun invoke(
        properties: Iterable<WifiProperty>,
        ipSubProperties: Collection<WifiProperty.IP.SubProperty>,
        remoteNetworkInfo: RemoteNetworkInfo
    ): List<WifiViewData> =
        properties.flatMap { property ->
            when (property) {
                is WifiProperty.NonIP -> property.viewData(remoteNetworkInfo.ipApiData)

                is WifiProperty.IP -> property.viewData(
                    systemIps = systemIpAddresses(connectivityManager),
                    publicIps = remoteNetworkInfo.publicIps,
                    subProperties = ipSubProperties
                )
            }
        }

    private fun WifiProperty.NonIP.viewData(ipApiData: IpApiData?): List<WifiViewData.NonIP> =
        viewData(
            values = getValues(ipApiData),
            resources = resources,
            makeViewData = { label, value -> WifiViewData.NonIP(value, label) }
        )

    @Suppress("DEPRECATION")
    private fun WifiProperty.NonIP.getValues(ipApiData: IpApiData?): List<String> {
        val dhcpInfo by lazy { wifiManager.dhcpInfo }
        val connectionInfo by lazy { wifiManager.connectionInfo }
        return buildList {
            when (this@getValues) {
                WifiProperty.NonIP.DNS -> {
                    add(
                        textualIPv4Representation(dhcpInfo.dns1)
                            ?: IpAddress.Version.V4.fallbackAddress
                    )
                    textualIPv4Representation(dhcpInfo.dns2)?.let { address ->
                        if (address != IpAddress.Version.V4.fallbackAddress) {
                            add(address)
                        }
                    }
                }

                WifiProperty.NonIP.SSID -> add(
                    connectionInfo.ssid
                        ?.replace("\"", "")
                        .takeIf { it != "<unknown ssid>" }
                        ?: resources.getString(R.string.no_location_access)
                )

                WifiProperty.NonIP.BSSID -> add(
                    connectionInfo.bssid
                        ?.takeIf { it != "02:00:00:00:00:00" }
                        ?: resources.getString(R.string.no_location_access)
                )

                WifiProperty.NonIP.Frequency -> add("${connectionInfo.frequency} MHz")
                WifiProperty.NonIP.Channel -> add(frequencyToChannel(connectionInfo.frequency).toString())
                WifiProperty.NonIP.LinkSpeed -> add("${connectionInfo.linkSpeed} Mbps")
                WifiProperty.NonIP.RSSI -> add("${connectionInfo.rssi} dBm")
                WifiProperty.NonIP.SignalStrength -> {
                    val rssi = connectionInfo.rssi
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

                WifiProperty.NonIP.Standard -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    add(
                        when (connectionInfo.wifiStandard) {
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

                WifiProperty.NonIP.Generation -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    add(
                        when (connectionInfo.wifiStandard) {
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

                WifiProperty.NonIP.Security -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    add(
                        when (connectionInfo.currentSecurityType) {
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

                WifiProperty.NonIP.Gateway -> add(
                    textualIPv4Representation(dhcpInfo.gateway)
                        ?: IpAddress.Version.V4.fallbackAddress
                )

                WifiProperty.NonIP.DHCP -> add(
                    textualIPv4Representation(dhcpInfo.serverAddress)
                        ?: IpAddress.Version.V4.fallbackAddress
                )

                WifiProperty.NonIP.NAT64Prefix -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    add(
                        connectivityManager.linkProperties?.nat64Prefix?.address?.hostAddress
                            ?: resources.getString(R.string.none)
                    )
                }

                WifiProperty.NonIP.Location -> ipApiData?.location?.let(::add)
                WifiProperty.NonIP.IpGpsLocation -> ipApiData?.gpsCoordinates?.let(::add)
                WifiProperty.NonIP.ISP -> ipApiData?.isp?.let(::add)
                WifiProperty.NonIP.ASN -> ipApiData?.asn?.let(::add)
            }
        }
    }

    private fun WifiProperty.IP.viewData(
        systemIps: List<IpAddress>,
        publicIps: Map<IpAddress.Version, IpAddress?>,
        subProperties: Collection<WifiProperty.IP.SubProperty>
    ): List<WifiViewData.IPProperty> =
        viewData(
            values = getAddresses(
                systemIps = systemIps,
                publicIps = publicIps,
                versionsToBeIncluded = buildSet {
                    if (this@viewData is WifiProperty.IP.V64) {
                        if (subProperties.contains(v4EnabledSubProperty)) {
                            add(IpAddress.Version.V4)
                        }
                        if (subProperties.contains(v6EnabledSubProperty)) {
                            add(IpAddress.Version.V6)
                        }
                    }
                }
            ),
            resources = resources,
            makeViewData = { label, ipAddress ->
                WifiViewData.IPProperty(
                    label = label,
                    value = ipAddress.hostAddressRepresentation,
                    subPropertyValues = buildList {
                        if (subProperties.contains(showSubnetMaskSubProperty)) {
                            ipAddress.asV4OrNull?.subnetMask?.let { subnetMask ->
                                add(subnetMask)
                            }
                        }
                        if (subProperties.contains(showPrefixLengthSubProperty) && ipAddress.prefixLength != null) {
                            add("/${ipAddress.prefixLength}")
                        }
                    }
                )
            }
        )

    private fun WifiProperty.IP.getAddresses(
        systemIps: List<IpAddress>,
        publicIps: Map<IpAddress.Version, IpAddress?>,
        versionsToBeIncluded: Set<IpAddress.Version>
    ): List<IpAddress> =
        when (this) {
            WifiProperty.IP.ULA -> systemIps.filter { it.asV6OrNull?.isUniqueLocal == true }
            WifiProperty.IP.GUA -> systemIps.filter { it.asV6OrNull?.isGlobalUnicast == true }
            WifiProperty.IP.Loopback -> systemIps.filterByVersionAndPredicate(versionsToBeIncluded) { it.isLoopback }
            WifiProperty.IP.SiteLocal -> systemIps.filterByVersionAndPredicate(versionsToBeIncluded) { it.isSiteLocal }
            WifiProperty.IP.LinkLocal -> systemIps.filterByVersionAndPredicate(versionsToBeIncluded) { it.isLinkLocal }
            WifiProperty.IP.Multicast -> systemIps.filterByVersionAndPredicate(versionsToBeIncluded) { it.isMulticast }
            WifiProperty.IP.Public -> versionsToBeIncluded.mapNotNull { version -> publicIps.getValue(version) }
        }
}

private fun <T, R : WifiViewData> WifiProperty.viewData(
    values: List<T>,
    resources: Resources,
    makeViewData: (String, T) -> R
): List<R> =
    buildList {
        val propertyLabel = resources.getString(
            run {
                if (this@viewData is WifiProperty.IP) {
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

private inline fun List<IpAddress>.filterByVersionAndPredicate(
    versionsToBeIncluded: Iterable<IpAddress.Version>,
    predicate: (IpAddress) -> Boolean
): List<IpAddress> =
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
