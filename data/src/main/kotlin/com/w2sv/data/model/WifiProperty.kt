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
    @StringRes val labelRes: Int,
    @ArrayRes val stringArrayRes: Int,
    val getValue: (WifiManager, ConnectivityManager) -> String,
    override val defaultValue: Boolean = true
) :
    DataStoreEntry.UniType<Boolean> {

    SSID(
        R.string.ssid,
        R.array.ssid,
        { wifiManager, _ ->
            wifiManager.connectionInfo.ssid.replace("\"", "")
        },
        false
    ),
    BSSID(
        R.string.bssid,
        R.array.bssid,
        { wifiManager, _ ->
            wifiManager.connectionInfo.bssid
        }
    ),
    IP(
        R.string.ipv4,
        R.array.ipv4,
        { _, connectivityManager ->
            connectivityManager
                .findLinkAddress { it.addressType == AddressType.IPv4 }
                ?.address
                ?.hostAddress
                ?: IPV4_FALLBACK_ADDRESS
        }
    ),
    Netmask(
        R.string.netmask,
        R.array.netmask,
        { _, connectivityManager ->
            connectivityManager
                .findLinkAddress { it.addressType == AddressType.IPv4 }
                ?.prefixLength
                ?.let { toNetmask(it) }
                ?: IPV4_FALLBACK_ADDRESS
        }
    ),
    IPv6Local(
        R.string.ipv6_local,
        R.array.ipv6_local,
        { _, connectivityManager ->
            connectivityManager
                .findLinkAddress { it.address.isLinkLocalAddress && it.addressType == AddressType.IPv6 }
                ?.address
                ?.hostAddress
                ?: IPV6_FALLBACK_ADDRESS
        }
    ),
    IPv6Public1(
        R.string.ipv6_public_first,
        R.array.ipv6_public_one,
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
        }
    ),
    IPv6Public2(
        R.string.ipv6_public_second,
        R.array.ipv6_public_two,
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
        }
    ),
    Frequency(
        R.string.frequency,
        R.array.frequency,
        { wifiManager, _ -> "${wifiManager.connectionInfo.frequency} MHz" }
    ),
    Channel(
        R.string.channel,
        R.array.channel,
        { wifiManager, _ -> frequencyToChannel(wifiManager.connectionInfo.frequency).toString() }
    ),
    LinkSpeed(
        R.string.linkspeed,
        R.array.linkspeed,
        { wifiManager, _ -> "${wifiManager.connectionInfo.linkSpeed} Mbps" }
    ),
    Gateway(
        R.string.gateway,
        R.array.gateway,
        { wifiManager, _ ->
            textualAddressRepresentation(wifiManager.dhcpInfo.gateway)
                ?: IPV4_FALLBACK_ADDRESS
        }
    ),
    DNS(
        R.string.dns,
        R.array.dns,
        { wifiManager, _ ->
            textualAddressRepresentation(wifiManager.dhcpInfo.dns1)
                ?: IPV4_FALLBACK_ADDRESS
        }
    ),
    DHCP(
        R.string.dhcp,
        R.array.dhcp,
        { wifiManager, _ ->
            textualAddressRepresentation(wifiManager.dhcpInfo.serverAddress)
                ?: IPV4_FALLBACK_ADDRESS
        }
    );

    override val preferencesKey: Preferences.Key<Boolean> = booleanPreferencesKey(name)
}

private const val IPV4_FALLBACK_ADDRESS = "0.0.0.0"
private const val IPV6_FALLBACK_ADDRESS = "::::::"