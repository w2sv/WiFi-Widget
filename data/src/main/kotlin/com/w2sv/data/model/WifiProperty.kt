package com.w2sv.data.model

import android.net.ConnectivityManager
import android.net.wifi.WifiManager
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
            R.string.ssid_description,
            "https://en.wikipedia.org/wiki/Service_set_(802.11_network)#SSID"
        ),
        { wifiManager, _ ->
            wifiManager.connectionInfo.ssid?.replace("\"", "") ?: DEFAULT_FALLBACK_VALUE
        },
        false
    ),
    BSSID(
        ViewData(
            R.string.bssid,
            R.string.bssid_description,
            "https://en.wikipedia.org/wiki/Service_set_(802.11_network)#BSSID"
        ),
        { wifiManager, _ ->
            wifiManager.connectionInfo.bssid ?: DEFAULT_FALLBACK_VALUE
        },
        false
    ),
    IP(
        ViewData(
            R.string.ipv4,
            R.string.ipv4_description,
            "https://en.wikipedia.org/wiki/IP_address"
        ),
        { _, connectivityManager ->
            connectivityManager
                .findLinkAddress { it.addressType == AddressType.IPv4 }
                ?.address
                ?.hostAddress
                ?: IPV4_FALLBACK_ADDRESS
        },
    ),
    Netmask(
        ViewData(
            R.string.netmask,
            R.string.netmask_description,
            "https://en.wikipedia.org/wiki/Subnetwork"
        ),
        { _, connectivityManager ->
            connectivityManager
                .findLinkAddress { it.addressType == AddressType.IPv4 }
                ?.prefixLength
                ?.let { toNetmask(it) }
                ?: IPV4_FALLBACK_ADDRESS
        },
    ),
    IPv6Local(
        ViewData(
            R.string.ipv6_local,
            R.string.ipv6_local_description,
            "https://en.wikipedia.org/wiki/IP_address"
        ),
        { _, connectivityManager ->
            connectivityManager
                .findLinkAddress { it.address.isLinkLocalAddress && it.addressType == AddressType.IPv6 }
                ?.address
                ?.hostAddress
                ?: IPV6_FALLBACK_ADDRESS
        },
    ),
    IPv6Public1(
        ViewData(
            R.string.ipv6_public_1,
            R.string.ipv6_public_description,
            "https://en.wikipedia.org/wiki/IP_address"
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
    ),
    IPv6Public2(
        ViewData(
            R.string.ipv6_public_2,
            R.string.ipv6_public_description,
            "https://en.wikipedia.org/wiki/IP_address"
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
    ),
    Frequency(
        ViewData(
            R.string.frequency,
            R.string.frequency,
            "https://en.wikipedia.org/wiki/List_of_WLAN_channels"
        ),
        { wifiManager, _ -> "${wifiManager.connectionInfo.frequency} MHz" },
    ),
    Channel(
        ViewData(
            R.string.channel,
            R.string.channel_description,
            "https://en.wikipedia.org/wiki/List_of_WLAN_channels"
        ),
        { wifiManager, _ -> frequencyToChannel(wifiManager.connectionInfo.frequency).toString() },
    ),
    LinkSpeed(
        ViewData(
            R.string.link_speed,
            R.string.link_speed_description,
            null
        ),
        { wifiManager, _ -> "${wifiManager.connectionInfo.linkSpeed} Mbps" },
    ),
    Gateway(
        ViewData(
            R.string.gateway,
            R.string.gateway_description,
            "https://en.wikipedia.org/wiki/Gateway_(telecommunications)#Network_gateway"
        ),
        { wifiManager, _ ->
            textualAddressRepresentation(wifiManager.dhcpInfo.gateway)
                ?: IPV4_FALLBACK_ADDRESS
        },
    ),
    DNS(
        ViewData(
            R.string.dns,
            R.string.dns_description,
            "https://en.wikipedia.org/wiki/Domain_Name_System"
        ),
        { wifiManager, _ ->
            textualAddressRepresentation(wifiManager.dhcpInfo.dns1)
                ?: IPV4_FALLBACK_ADDRESS
        },
    ),
    DHCP(
        ViewData(
            R.string.dhcp,
            R.string.dhcp_description,
            "https://en.wikipedia.org/wiki/Dynamic_Host_Configuration_Protocol"
        ),
        { wifiManager, _ ->
            textualAddressRepresentation(wifiManager.dhcpInfo.serverAddress)
                ?: IPV4_FALLBACK_ADDRESS
        },
    );

    data class ViewData(
        @StringRes val labelRes: Int,
        @StringRes val descriptionRes: Int,
        val learnMoreUrl: String?
    )

    override val preferencesKey: Preferences.Key<Boolean> = booleanPreferencesKey(name)
}

private const val DEFAULT_FALLBACK_VALUE = "Couldn't retrieve"
private const val IPV4_FALLBACK_ADDRESS = "0.0.0.0"
private const val IPV6_FALLBACK_ADDRESS = "::::::"