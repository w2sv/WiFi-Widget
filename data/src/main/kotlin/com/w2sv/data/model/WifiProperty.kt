package com.w2sv.data.model

import android.net.ConnectivityManager
import android.net.wifi.WifiManager
import androidx.annotation.ArrayRes
import androidx.annotation.StringRes
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import com.w2sv.androidutils.datastorage.datastore.preferences.DataStoreEntry
import com.w2sv.data.AddressType
import com.w2sv.data.R
import com.w2sv.data.addressType
import com.w2sv.data.findLinkAddress
import com.w2sv.data.frequencyToChannel
import com.w2sv.data.getPublicIPv6Addresses
import com.w2sv.data.textualAddressRepresentation
import com.w2sv.data.toNetmask

@Suppress("DEPRECATION")
enum class WifiProperty(
    val viewData: ViewData,
    val getValue: (WifiManager, ConnectivityManager) -> String,
    override val defaultValue: Boolean = true
) :
    DataStoreEntry.UniType<Boolean> {

    SSID(
        ViewData(
            R.string.ssid,
            R.array.ssid
        ),
        { wifiManager, _ ->
            wifiManager.connectionInfo.ssid?.replace("\"", "") ?: DEFAULT_FALLBACK_VALUE
        },
        false
    ),
    BSSID(
        ViewData(
            R.string.bssid,
            R.array.bssid
        ),
        { wifiManager, _ ->
            wifiManager.connectionInfo.bssid ?: DEFAULT_FALLBACK_VALUE
        },
        true
    ),
    IP(
        ViewData(
            R.string.ipv4,
            R.array.ipv4
        ),
        { _, connectivityManager ->
            connectivityManager
                .findLinkAddress { it.addressType == AddressType.IPv4 }
                ?.address
                ?.hostAddress
                ?: IPV4_FALLBACK_ADDRESS
        },
        true
    ),
    Netmask(
        ViewData(
            R.string.netmask,
            R.array.netmask
        ),
        { _, connectivityManager ->
            connectivityManager
                .findLinkAddress { it.addressType == AddressType.IPv4 }
                ?.prefixLength
                ?.let { toNetmask(it) }
                ?: IPV4_FALLBACK_ADDRESS
        },
        true
    ),
    IPv6Local(
        ViewData(
            R.string.ipv6_local,
            R.array.ipv6_local
        ),
        { _, connectivityManager ->
            connectivityManager
                .findLinkAddress { it.address.isLinkLocalAddress && it.addressType == AddressType.IPv6 }
                ?.address
                ?.hostAddress
                ?: IPV6_FALLBACK_ADDRESS
        },
        true
    ),
    IPv6Public1(
        ViewData(
            R.string.ipv6_public_1,
            R.array.ipv6_public_1
        ),
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
        true
    ),
    IPv6Public2(
        ViewData(
            R.string.ipv6_public_2,
            R.array.ipv6_public_2
        ),
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
        true
    ),
    Frequency(
        ViewData(
            R.string.frequency,
            R.array.frequency
        ),
        { wifiManager, _ -> "${wifiManager.connectionInfo.frequency} MHz" },
        true
    ),
    Channel(
        ViewData(
            R.string.channel,
            R.array.channel
        ),
        { wifiManager, _ -> frequencyToChannel(wifiManager.connectionInfo.frequency).toString() },
        true
    ),
    LinkSpeed(
        ViewData(
            R.string.link_speed,
            R.array.link_speed
        ),
        { wifiManager, _ -> "${wifiManager.connectionInfo.linkSpeed} Mbps" },
        true
    ),
    Gateway(
        ViewData(
            R.string.gateway,
            R.array.gateway
        ),
        { wifiManager, _ ->
            textualAddressRepresentation(wifiManager.dhcpInfo.gateway)
                ?: IPV4_FALLBACK_ADDRESS
        },
        true
    ),
    DNS(
        ViewData(
            R.string.dns,
            R.array.dns
        ),
        { wifiManager, _ ->
            textualAddressRepresentation(wifiManager.dhcpInfo.dns1)
                ?: IPV4_FALLBACK_ADDRESS
        },
        true
    ),
    DHCP(
        ViewData(
            R.string.dhcp,
            R.array.dhcp
        ),
        { wifiManager, _ ->
            textualAddressRepresentation(wifiManager.dhcpInfo.serverAddress)
                ?: IPV4_FALLBACK_ADDRESS
        },
        true
    );

    data class ViewData(@StringRes val labelRes: Int, @ArrayRes val arrayRes: Int)

    override val preferencesKey: Preferences.Key<Boolean> = booleanPreferencesKey(name)
}

private const val DEFAULT_FALLBACK_VALUE = "Couldn't retrieve"
private const val IPV4_FALLBACK_ADDRESS = "0.0.0.0"
private const val IPV6_FALLBACK_ADDRESS = "::::::"