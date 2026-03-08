package com.w2sv.networking.propertyviewdata.values

import android.net.wifi.ScanResult
import android.net.wifi.WifiInfo
import android.os.Build
import androidx.annotation.VisibleForTesting
import com.w2sv.common.txt
import com.w2sv.core.networking.R
import com.w2sv.domain.model.wifiproperty.WifiProperty
import com.w2sv.domain.model.wifiproperty.settings.IpSetting
import com.w2sv.networking.propertyviewdata.WifiSnapshot
import java.net.InetAddress
import java.nio.ByteBuffer
import java.nio.ByteOrder

@Suppress("DEPRECATION")
internal fun WifiProperty.resolve(
    snapshot: WifiSnapshot,
    enabledIpSettings: (WifiProperty.IpProperty) -> List<IpSetting>
): List<WifiPropertyValue> =
    buildList {
        val connectionInfo = snapshot.connectionInfo
        val dhcpInfo = snapshot.dhcpInfo
        val linkProperties = snapshot.linkProperties
        val ipApiData = snapshot.ipApiData

        when (this@resolve) {
            WifiProperty.SSID -> add(
                WifiPropertyValue(
                    connectionInfo.ssid
                        .replace("\"", "")
                        .takeIf { it != "<unknown ssid>" }
                        ?.txt
                        ?: R.string.no_location_access.txt
                )
            )

            WifiProperty.BSSID -> add(
                WifiPropertyValue(
                    connectionInfo.bssid
                        .takeIf { it != "02:00:00:00:00:00" }
                        ?.txt
                        ?: R.string.no_location_access.txt
                )
            )

            WifiProperty.Frequency -> add(WifiPropertyValue("${connectionInfo.frequency} MHz".txt))
            WifiProperty.Channel -> add(WifiPropertyValue(frequencyToChannel(connectionInfo.frequency).toString().txt))
            WifiProperty.LinkSpeed -> add(WifiPropertyValue("${connectionInfo.linkSpeed} Mbps".txt))
            WifiProperty.RSSI -> add(WifiPropertyValue("${connectionInfo.rssi} dBm".txt))

            WifiProperty.SignalStrength -> {
                val rssi = connectionInfo.rssi
                add(
                    WifiPropertyValue(
                        when {
                            rssi >= -60 -> R.string.excellent
                            rssi >= -70 -> R.string.good
                            rssi >= -80 -> R.string.fair
                            rssi >= -90 -> R.string.weak
                            else -> R.string.poor
                        }.txt
                    )
                )
            }

            WifiProperty.Standard -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                add(
                    WifiPropertyValue(
                        when (connectionInfo.wifiStandard) {
                            ScanResult.WIFI_STANDARD_11AC -> "802.11ac".txt
                            ScanResult.WIFI_STANDARD_11AD -> "802.11ad".txt
                            ScanResult.WIFI_STANDARD_11AX -> "802.11ax".txt
                            ScanResult.WIFI_STANDARD_11BE -> "802.11be".txt
                            ScanResult.WIFI_STANDARD_11N -> "802.11n".txt
                            ScanResult.WIFI_STANDARD_LEGACY -> "802.11a/b/g".txt
                            else -> R.string.unknown.txt
                        }
                    )
                )
            }

            WifiProperty.Generation -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                add(
                    WifiPropertyValue(
                        when (connectionInfo.wifiStandard) {
                            ScanResult.WIFI_STANDARD_11AC -> "Wi-Fi 5".txt
                            ScanResult.WIFI_STANDARD_11AD -> "WiGig".txt
                            ScanResult.WIFI_STANDARD_11AX -> "Wi-Fi 6".txt
                            ScanResult.WIFI_STANDARD_11BE -> "Wi-Fi 7".txt
                            ScanResult.WIFI_STANDARD_11N -> "Wi-Fi 4".txt
                            ScanResult.WIFI_STANDARD_LEGACY -> "Legacy".txt
                            else -> R.string.unknown.txt
                        }
                    )
                )
            }

            WifiProperty.Security -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                add(
                    WifiPropertyValue(
                        when (connectionInfo.currentSecurityType) {
                            WifiInfo.SECURITY_TYPE_OPEN -> "Open".txt
                            WifiInfo.SECURITY_TYPE_WEP -> "WEP".txt
                            WifiInfo.SECURITY_TYPE_PSK -> "PSK (WPA/WPA2)".txt
                            WifiInfo.SECURITY_TYPE_EAP -> "EAP (WPA/WPA2)".txt
                            WifiInfo.SECURITY_TYPE_SAE -> "SAE (WPA3)".txt
                            WifiInfo.SECURITY_TYPE_OWE -> "OWE (WPA3)".txt
                            else -> R.string.unknown.txt
                        }
                    )
                )
            }

            WifiProperty.DNS -> listOf(dhcpInfo.dns1, dhcpInfo.dns2).forEach { dns ->
                addIfNotNull(textualIPv4Representation(dns))
            }

            WifiProperty.Gateway -> addIfNotNull(textualIPv4Representation(dhcpInfo.gateway))
            WifiProperty.DHCP -> addIfNotNull(textualIPv4Representation(dhcpInfo.serverAddress))
            WifiProperty.NAT64Prefix -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                addIfNotNull(linkProperties?.nat64Prefix?.address?.hostAddress)
            }

            WifiProperty.Location -> addIfNotNull(ipApiData?.location)
            WifiProperty.IpGpsLocation -> addIfNotNull(ipApiData?.gpsCoordinates)
            WifiProperty.ISP -> addIfNotNull(ipApiData?.isp)
            WifiProperty.ASN -> addIfNotNull(ipApiData?.asn)
            is WifiProperty.IpProperty -> addAll(
                resolve(
                    publicIps = snapshot.publicIps,
                    systemIps = snapshot.systemIps,
                    enabledIpSettings = enabledIpSettings
                )
            )
        }
    }

private fun MutableList<WifiPropertyValue>.addIfNotNull(value: String?) {
    value?.run { add(WifiPropertyValue(txt)) }
}

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
