package com.w2sv.common.enums

import android.net.ConnectivityManager
import android.net.wifi.WifiManager
import androidx.annotation.ArrayRes
import androidx.annotation.StringRes
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import com.w2sv.common.IPAddressType
import com.w2sv.common.R
import com.w2sv.common.datastore.DataStoreVariable
import com.w2sv.common.findLinkAddress
import com.w2sv.common.frequencyToChannel
import com.w2sv.common.getPublicIPv6Addresses
import com.w2sv.common.ipAddressType
import com.w2sv.common.textualAddressRepresentation

@Suppress("DEPRECATION")
enum class WifiProperty(
    @StringRes val labelRes: Int,
    @ArrayRes val infoStringArrayRes: Int,
    val getValue: (WifiManager, ConnectivityManager) -> String,
    override val defaultValue: Boolean = true
) :
    DataStoreVariable<Boolean> {

    SSID(
        R.string.ssid,
        R.array.ssid,
        { wifiManager, _ ->
            wifiManager.connectionInfo.ssid.replace("\"", "")
        },
        false
    ),
    IP(
        R.string.ipv4,
        R.array.ip,
        { _, connectivityManager ->
            connectivityManager
                .findLinkAddress { it.ipAddressType == IPAddressType.V4 }
                ?.address
                ?.hostAddress
                ?: IPAddressType.V6.fallbackAddress
        }
    ),
    Netmask(
        R.string.netmask,
        R.array.netmask,
        { _, connectivityManager ->
            connectivityManager
                .findLinkAddress { it.ipAddressType == IPAddressType.V4 }
                ?.prefixLength
                ?.let { com.w2sv.common.toNetmask(it) }
                ?: IPAddressType.V4.fallbackAddress
        }
    ),
    IPv6Local(
        R.string.ipv6_local,
        R.array.ip,
        { _, connectivityManager ->
            connectivityManager
                .findLinkAddress { it.address.isLinkLocalAddress && it.ipAddressType == IPAddressType.V6 }
                ?.address
                ?.hostAddress
                ?: IPAddressType.V6.fallbackAddress
        }
    ),
    IPv6Public1(
        R.string.ipv6_public,
        R.array.ip,
        { _, connectivityManager ->
            connectivityManager
                .getPublicIPv6Addresses()
                ?.get(0)
                ?.hostAddress
                ?: IPAddressType.V6.fallbackAddress
        }
    ),
    IPv6Public2(
        R.string.ipv6_public,
        R.array.ip,
        { _, connectivityManager ->
            connectivityManager
                .getPublicIPv6Addresses()
                ?.get(1)
                ?.hostAddress
                ?: IPAddressType.V6.fallbackAddress
        }
    ),
    Frequency(
        R.string.frequency,
        R.array.frequency,
        { wifiManager, _ -> quantityRepresentation(wifiManager.connectionInfo.frequency, "MHz") }
    ),
    Channel(
        R.string.channel,
        R.array.channel,
        { wifiManager, _ -> frequencyToChannel(wifiManager.connectionInfo.frequency).toString() }
    ),
    Linkspeed(
        R.string.linkspeed,
        R.array.linkspeed,
        { wifiManager, _ -> quantityRepresentation(wifiManager.connectionInfo.linkSpeed, "Mbps") }
    ),
    Gateway(
        R.string.gateway,
        R.array.gateway,
        { wifiManager, _ ->
            textualAddressRepresentation(wifiManager.dhcpInfo.gateway)
                ?: IPAddressType.V4.fallbackAddress
        }
    ),
    DNS(
        R.string.dns,
        R.array.dns,
        { wifiManager, _ ->
            textualAddressRepresentation(wifiManager.dhcpInfo.dns1)
                ?: IPAddressType.V4.fallbackAddress
        }
    ),
    DHCP(
        R.string.dhcp,
        R.array.dhcp,
        { wifiManager, _ ->
            textualAddressRepresentation(wifiManager.dhcpInfo.serverAddress)
                ?: IPAddressType.V4.fallbackAddress
        }
    );

    override val preferencesKey: Preferences.Key<Boolean> = booleanPreferencesKey(name)
}

private fun quantityRepresentation(quantity: Number, unit: String): String =
    "$quantity $unit"