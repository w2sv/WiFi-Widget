package com.w2sv.data.model

import android.content.Context
import androidx.annotation.StringRes
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import com.w2sv.androidutils.datastorage.datastore.preferences.DataStoreEntry
import com.w2sv.data.R
import com.w2sv.data.networking.IPAddress
import com.w2sv.data.networking.connectivityManager
import com.w2sv.data.networking.frequencyToChannel
import com.w2sv.data.networking.getIPAddresses
import com.w2sv.data.networking.wifiManager

@Suppress("DEPRECATION")
enum class WifiProperty(
    val viewData: ViewData,
    val getValue: (ValueGetterResources) -> Value,
    override val defaultValue: Boolean = true
) :
    DataStoreEntry.UniType<Boolean> {

    SSID(
        ViewData(
            R.string.ssid,
            R.string.ssid_description,
            "https://en.wikipedia.org/wiki/Service_set_(802.11_network)#SSID"
        ),
        {
            Value.Singular(
                it.wifiManager.connectionInfo.ssid?.replace("\"", "") ?: DEFAULT_FALLBACK_VALUE
            )
        },
        false
    ),
    BSSID(
        ViewData(
            R.string.bssid,
            R.string.bssid_description,
            "https://en.wikipedia.org/wiki/Service_set_(802.11_network)#BSSID"
        ),
        {
            Value.Singular(
                it.wifiManager.connectionInfo.bssid ?: DEFAULT_FALLBACK_VALUE
            )
        },
        false
    ),
    IPv4(
        ViewData(
            R.string.ipv4,
            R.string.ipv4_description,
            "https://en.wikipedia.org/wiki/IP_address"
        ),
        {
            Value.Singular(
                it.ipAddresses
                    ?.find { address -> address.type == IPAddress.Type.V4 }
                    ?.textualRepresentation
                    ?: IPAddress.Type.V4.fallbackAddress
            )
        },
    ),
    IPv6Local(
        ViewData(
            R.string.ipv6_local,
            R.string.ipv6_local_description,
            "https://en.wikipedia.org/wiki/IP_address"
        ),
        {
            Value.Singular(
                it.ipAddresses
                    ?.find { address -> address.type == IPAddress.Type.V6 && address.isLocal }
                    ?.textualRepresentation
                    ?: IPAddress.Type.V6.fallbackAddress
            )
        },
    ),
    IPv6Public(
        ViewData(
            R.string.ipv6_public,
            R.string.ipv6_public_description,
            "https://en.wikipedia.org/wiki/IP_address"
        ),
        {
            it.ipAddresses
                ?.filter { address -> address.type == IPAddress.Type.V6 && !address.isLocal }
                ?.let { addresses ->
                    Value.IPAddresses(addresses)
                }
                ?: Value.Singular(IPAddress.Type.V6.fallbackAddress)
        },
    ),
    Frequency(
        ViewData(
            R.string.frequency,
            R.string.frequency_description,
            "https://en.wikipedia.org/wiki/List_of_WLAN_channels"
        ),
        { Value.Singular("${it.wifiManager.connectionInfo.frequency} MHz") },
    ),
    Channel(
        ViewData(
            R.string.channel,
            R.string.channel_description,
            "https://en.wikipedia.org/wiki/List_of_WLAN_channels"
        ),
        { Value.Singular(frequencyToChannel(it.wifiManager.connectionInfo.frequency).toString()) },
    ),
    LinkSpeed(
        ViewData(
            R.string.link_speed,
            R.string.link_speed_description,
            null
        ),
        { Value.Singular("${it.wifiManager.connectionInfo.linkSpeed} Mbps") },
    ),
    Gateway(
        ViewData(
            R.string.gateway,
            R.string.gateway_description,
            "https://en.wikipedia.org/wiki/Gateway_(telecommunications)#Network_gateway"
        ),
        {
            Value.Singular(
                com.w2sv.data.networking.textualIPv4Representation(it.wifiManager.dhcpInfo.gateway)
                    ?: IPAddress.Type.V4.fallbackAddress
            )
        },
    ),
    DNS(
        ViewData(
            R.string.dns,
            R.string.dns_description,
            "https://en.wikipedia.org/wiki/Domain_Name_System"
        ),
        {
            Value.Singular(
                com.w2sv.data.networking.textualIPv4Representation(it.wifiManager.dhcpInfo.dns1)
                    ?: IPAddress.Type.V4.fallbackAddress
            )
        },
    ),
    DHCP(
        ViewData(
            R.string.dhcp,
            R.string.dhcp_description,
            "https://en.wikipedia.org/wiki/Dynamic_Host_Configuration_Protocol"
        ),
        {
            Value.Singular(
                com.w2sv.data.networking.textualIPv4Representation(it.wifiManager.dhcpInfo.serverAddress)
                    ?: IPAddress.Type.V4.fallbackAddress
            )
        },
    );

    data class ViewData(
        @StringRes val labelRes: Int,
        @StringRes val descriptionRes: Int,
        val learnMoreUrl: String?
    )

    sealed interface Value {
        class Singular(val value: String) : Value
        class IPAddresses(val addresses: List<IPAddress>) : Value
    }

    class ValueGetterResources(val context: Context) {
        val wifiManager by lazy {
            context.wifiManager
        }

        val ipAddresses: List<IPAddress>? by lazy {
            context.connectivityManager.getIPAddresses()
        }
    }

    override val preferencesKey: Preferences.Key<Boolean> = booleanPreferencesKey(name)
}

private const val DEFAULT_FALLBACK_VALUE = "Couldn't retrieve"