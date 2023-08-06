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
            Value.getForIPProperty(
                it.ipAddresses,
                IPAddress.Type.V4
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
            com.w2sv.data.model.WifiProperty.Value.getForIPProperty(
                it.ipAddresses,
                com.w2sv.data.networking.IPAddress.Type.V6,
                { it.isLocal }
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
            com.w2sv.data.model.WifiProperty.Value.getForIPProperty(
                it.ipAddresses,
                com.w2sv.data.networking.IPAddress.Type.V6,
                { !it.isLocal }
            )
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

        companion object {
            fun getForIPProperty(
                ipAddresses: List<IPAddress>?,
                type: IPAddress.Type,
                additionalFilterPredicate: (IPAddress) -> Boolean = { true }
            ): Value =
                ipAddresses
                    ?.filter { it.type == type && additionalFilterPredicate(it) }
                    ?.let { addresses ->
                        IPAddresses(addresses)
                    }
                    ?: Singular(type.fallbackAddress)
        }
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