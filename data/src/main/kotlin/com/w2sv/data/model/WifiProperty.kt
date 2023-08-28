package com.w2sv.data.model

import android.content.Context
import android.net.ConnectivityManager
import android.net.wifi.WifiManager
import androidx.annotation.StringRes
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import com.w2sv.androidutils.datastorage.datastore.preferences.DataStoreEntry
import com.w2sv.androidutils.services.getConnectivityManager
import com.w2sv.androidutils.services.getWifiManager
import com.w2sv.data.R
import com.w2sv.data.networking.IPAddress
import com.w2sv.data.networking.frequencyToChannel
import com.w2sv.data.networking.getIPAddresses
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

@Suppress("DEPRECATION")
sealed class WifiProperty(
    val viewData: ViewData,
    val getValue: (ValueGetterResources) -> Value,
    isEnabledDataStoreEntry: DataStoreEntry.UniType<Boolean>,
) : DataStoreEntry.UniType<Boolean> by isEnabledDataStoreEntry {

    sealed class IPProperty(
        viewData: ViewData,
        getValue: (ValueGetterResources) -> Value,
        preferencesKey: Preferences.Key<Boolean>,
        val prefixLengthSubProperty: SubProperty,
    ) : WifiProperty(
        viewData,
        getValue,
        DataStoreEntry.UniType.Impl(preferencesKey, defaultValue = false),
    ) {
        open val subProperties: List<SubProperty> = listOf(prefixLengthSubProperty)

        class SubProperty(
            val viewData: ViewData,
            override val preferencesKey: Preferences.Key<Boolean>,
            override val defaultValue: Boolean = true,
        ) : DataStoreEntry.UniType<Boolean>

        companion object {
            fun values(): Array<IPProperty> {
                return arrayOf(
                    IPv4,
                    IPv6,
                )
            }

            const val LEARN_MORE_URL = "https://en.wikipedia.org/wiki/IP_address"
        }
    }

    data class ViewData(
        @StringRes val labelRes: Int,
        @StringRes val descriptionRes: Int,
        val learnMoreUrl: String? = null,
    )

    sealed interface Value {
        class Singular(val value: String) : Value
        class IPAddresses(val property: IPProperty, val addresses: List<IPAddress>) : Value

        companion object {
            fun getForIPProperty(
                property: IPProperty,
                ipAddresses: List<IPAddress>,
                type: IPAddress.Type,
            ): Value =
                ipAddresses
                    .filter { it.type == type }
                    .run {
                        if (isEmpty()) {
                            Singular(type.fallbackAddress)
                        } else {
                            IPAddresses(property, this)
                        }
                    }
        }
    }

    class ValueGetterResources(
        val wifiManager: WifiManager,
        val ipAddresses: List<IPAddress>,
    ) {
        constructor(wifiManager: WifiManager, connectivityManager: ConnectivityManager) : this(
            wifiManager,
            connectivityManager.getIPAddresses(),
        )

        constructor(context: Context) : this(
            context.getWifiManager(),
            context.getConnectivityManager().getIPAddresses(),
        )

        class Provider @Inject constructor(@ApplicationContext private val context: Context) {

            private val wifiManager by lazy { context.getWifiManager() }
            private val connectivityManager by lazy { context.getConnectivityManager() }

            fun provide(): ValueGetterResources =
                ValueGetterResources(wifiManager, connectivityManager)
        }
    }

    data object SSID : WifiProperty(
        ViewData(
            R.string.ssid,
            R.string.ssid_description,
            "https://en.wikipedia.org/wiki/Service_set_(802.11_network)#SSID",
        ),
        {
            Value.Singular(
                it.wifiManager.connectionInfo.ssid?.replace("\"", "") ?: DEFAULT_FALLBACK_VALUE,
            )
//            Value.Singular(
//                "Your-SSID"
//            )
        },
        DataStoreEntry.UniType.Impl(
            preferencesKey = booleanPreferencesKey("SSID"),
            defaultValue = false,
        ),
    )

    data object BSSID : WifiProperty(
        ViewData(
            R.string.bssid,
            R.string.bssid_description,
            "https://en.wikipedia.org/wiki/Service_set_(802.11_network)#BSSID",
        ),
        {
            Value.Singular(
                it.wifiManager.connectionInfo.bssid ?: DEFAULT_FALLBACK_VALUE,
            )
//            Value.Singular(
//                "34:8A:7B:2F:94:1C"
//            )
        },
        DataStoreEntry.UniType.Impl(
            preferencesKey = booleanPreferencesKey("BSSID"),
            defaultValue = false,
        ),
    )

    data object IPv4 :
        IPProperty(
            ViewData(
                R.string.ipv4,
                R.string.ipv4_description,
                LEARN_MORE_URL,
            ),
            {
                Value.getForIPProperty(
                    IPv4,
                    it.ipAddresses,
                    IPAddress.Type.V4,
                )
            },
            preferencesKey = booleanPreferencesKey("IPv4"),
            SubProperty(
                prefixLengthViewData,
                booleanPreferencesKey("IPv4.PrefixLength"),
            ),
        )

    data object IPv6 :
        IPProperty(
            viewData = ViewData(
                labelRes = R.string.ipv6,
                descriptionRes = R.string.ipv6_description,
                learnMoreUrl = LEARN_MORE_URL,
            ),
            getValue = {
                Value.getForIPProperty(
                    property = IPv6,
                    ipAddresses = it.ipAddresses,
                    type = IPAddress.Type.V6,
                )
//                Value.IPAddresses(
//                    IPv6,
//                    listOf(
//                        IPAddress(
//                            prefixLength = 64,
//                            hostAddress = "fe80::abcd:1234:5678:ef01",
//                            localAttributes = IPAddress.LocalAttributes(
//                                linkLocal = true,
//                                siteLocal = false,
//                                anyLocal = false
//                            ),
//                            isLoopback = false,
//                            isMulticast = false
//                        ),
//                        IPAddress(
//                            prefixLength = 64,
//                            hostAddress = "2001:0db8:85a3:0000:0000:8a2e:0370:7334",
//                            localAttributes = IPAddress.LocalAttributes(
//                                linkLocal = false,
//                                siteLocal = false,
//                                anyLocal = false
//                            ),
//                            isLoopback = false,
//                            isMulticast = false
//                        )
//                    )
//                )
            },
            preferencesKey = booleanPreferencesKey("IPv6"),
            prefixLengthSubProperty = SubProperty(
                viewData = prefixLengthViewData,
                preferencesKey = booleanPreferencesKey("IPv6.PrefixLength"),
            ),
        ) {
        val includeLocal =
            SubProperty(
                ViewData(R.string.include_local, R.string.include_local_description),
                booleanPreferencesKey("IPv6.IncludeLocal"),
            )
        val includePublic =
            SubProperty(
                ViewData(R.string.include_public, R.string.include_public_description),
                booleanPreferencesKey("IPv6.IncludePublic"),
            )

        override val subProperties: List<SubProperty> = listOf(
            includeLocal,
            includePublic,
            prefixLengthSubProperty,
        )
    }

    data object Frequency : WifiProperty(
        ViewData(
            R.string.frequency,
            R.string.frequency_description,
            "https://en.wikipedia.org/wiki/List_of_WLAN_channels",
        ),
        { Value.Singular("${it.wifiManager.connectionInfo.frequency} MHz") },
        DataStoreEntry.UniType.Impl(
            preferencesKey = booleanPreferencesKey("Frequency"),
            defaultValue = true,
        ),
    )

    data object Channel : WifiProperty(
        ViewData(
            R.string.channel,
            R.string.channel_description,
            "https://en.wikipedia.org/wiki/List_of_WLAN_channels",
        ),
        { Value.Singular(frequencyToChannel(it.wifiManager.connectionInfo.frequency).toString()) },
        DataStoreEntry.UniType.Impl(
            preferencesKey = booleanPreferencesKey("Channel"),
            defaultValue = true,
        ),
    )

    data object LinkSpeed : WifiProperty(
        ViewData(
            R.string.link_speed,
            R.string.link_speed_description,
            null,
        ),
        { Value.Singular("${it.wifiManager.connectionInfo.linkSpeed} Mbps") },
        DataStoreEntry.UniType.Impl(
            preferencesKey = booleanPreferencesKey("LinkSpeed"),
            defaultValue = true,
        ),
    )

    data object Gateway : WifiProperty(
        ViewData(
            R.string.gateway,
            R.string.gateway_description,
            "https://en.wikipedia.org/wiki/Gateway_(telecommunications)#Network_gateway",
        ),
        {
            Value.Singular(
                com.w2sv.data.networking.textualIPv4Representation(it.wifiManager.dhcpInfo.gateway)
                    ?: IPAddress.Type.V4.fallbackAddress,
            )
        },
        DataStoreEntry.UniType.Impl(
            preferencesKey = booleanPreferencesKey("Gateway"),
            defaultValue = true,
        ),
    )

    data object DNS : WifiProperty(
        ViewData(
            R.string.dns,
            R.string.dns_description,
            "https://en.wikipedia.org/wiki/Domain_Name_System",
        ),
        {
            Value.Singular(
                com.w2sv.data.networking.textualIPv4Representation(it.wifiManager.dhcpInfo.dns1)
                    ?: IPAddress.Type.V4.fallbackAddress,
            )
        },
        DataStoreEntry.UniType.Impl(
            preferencesKey = booleanPreferencesKey("DNS"),
            defaultValue = true,
        ),
    )

    data object DHCP : WifiProperty(
        ViewData(
            R.string.dhcp,
            R.string.dhcp_description,
            "https://en.wikipedia.org/wiki/Dynamic_Host_Configuration_Protocol",
        ),
        {
            Value.Singular(
                com.w2sv.data.networking.textualIPv4Representation(it.wifiManager.dhcpInfo.serverAddress)
                    ?: IPAddress.Type.V4.fallbackAddress,
            )
        },
        DataStoreEntry.UniType.Impl(
            preferencesKey = booleanPreferencesKey("DHCP"),
            defaultValue = true,
        ),
    )

    companion object {
        fun values(): Array<WifiProperty> {
            return arrayOf(
                SSID,
                BSSID,
                IPv4,
                IPv6,
                Frequency,
                Channel,
                LinkSpeed,
                Gateway,
                DNS,
                DHCP,
            )
        }
    }
}

private val prefixLengthViewData = WifiProperty.ViewData(
    R.string.show_prefix_length,
    R.string.prefix_length_description,
    "https://www.ibm.com/docs/en/ts3500-tape-library?topic=formats-subnet-masks-ipv4-prefixes-ipv6",
)

private const val DEFAULT_FALLBACK_VALUE = "Couldn't retrieve"
