package com.w2sv.data.model

import android.net.ConnectivityManager
import android.net.wifi.WifiManager
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import com.w2sv.androidutils.datastorage.datastore.preferences.DataStoreEntry
import com.w2sv.data.AddressType
import com.w2sv.data.addressType
import com.w2sv.data.findLinkAddress
import com.w2sv.data.frequencyToChannel
import com.w2sv.data.getPublicIPv6Addresses
import com.w2sv.data.textualAddressRepresentation
import com.w2sv.data.toNetmask

@Suppress("DEPRECATION")
enum class WifiProperty(
    val getValue: (WifiManager, ConnectivityManager) -> String,
    override val defaultValue: Boolean = true
) :
    DataStoreEntry.UniType<Boolean> {

    SSID(
        { wifiManager, _ ->
            wifiManager.connectionInfo.ssid.replace("\"", "")
        },
        false
    ),
    BSSID(
        { wifiManager, _ ->
            wifiManager.connectionInfo.bssid
        },
    ),
    IP(
        { _, connectivityManager ->
            connectivityManager
                .findLinkAddress { it.addressType == AddressType.IPv4 }
                ?.address
                ?.hostAddress
                ?: IPV4_FALLBACK_ADDRESS
        },
    ),
    Netmask(
        { _, connectivityManager ->
            connectivityManager
                .findLinkAddress { it.addressType == AddressType.IPv4 }
                ?.prefixLength
                ?.let { toNetmask(it) }
                ?: IPV4_FALLBACK_ADDRESS
        },
    ),
    IPv6Local(
        { _, connectivityManager ->
            connectivityManager
                .findLinkAddress { it.address.isLinkLocalAddress && it.addressType == AddressType.IPv6 }
                ?.address
                ?.hostAddress
                ?: IPV6_FALLBACK_ADDRESS
        },
    ),
    IPv6Public1(
        { _, connectivityManager ->
            try {
                connectivityManager
                    .getPublicIPv6Addresses()
                    ?.get(0)
                    ?.hostAddress
                    ?: IPV6_FALLBACK_ADDRESS
            } catch (e: IndexOutOfBoundsException) {
                IPV6_FALLBACK_ADDRESS
            }
        },
    ),
    IPv6Public2(
        { _, connectivityManager ->
            try {
                connectivityManager
                    .getPublicIPv6Addresses()
                    ?.get(1)
                    ?.hostAddress
                    ?: IPV6_FALLBACK_ADDRESS
            } catch (e: IndexOutOfBoundsException) {
                IPV6_FALLBACK_ADDRESS
            }
        },
    ),
    Frequency(
        { wifiManager, _ -> "${wifiManager.connectionInfo.frequency} MHz" },
    ),
    Channel(
        { wifiManager, _ -> frequencyToChannel(wifiManager.connectionInfo.frequency).toString() },
    ),
    LinkSpeed(
        { wifiManager, _ -> "${wifiManager.connectionInfo.linkSpeed} Mbps" },
    ),
    Gateway(
        { wifiManager, _ ->
            textualAddressRepresentation(wifiManager.dhcpInfo.gateway)
                ?: IPV4_FALLBACK_ADDRESS
        },
    ),
    DNS(
        { wifiManager, _ ->
            textualAddressRepresentation(wifiManager.dhcpInfo.dns1)
                ?: IPV4_FALLBACK_ADDRESS
        },
    ),
    DHCP(
        { wifiManager, _ ->
            textualAddressRepresentation(wifiManager.dhcpInfo.serverAddress)
                ?: IPV4_FALLBACK_ADDRESS
        },
    );

    override val preferencesKey: Preferences.Key<Boolean> = booleanPreferencesKey(name)
}

private const val IPV4_FALLBACK_ADDRESS = "0.0.0.0"
private const val IPV6_FALLBACK_ADDRESS = "::::::"