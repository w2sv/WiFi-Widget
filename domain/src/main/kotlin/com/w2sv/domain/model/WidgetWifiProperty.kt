package com.w2sv.domain.model

import android.os.Build
import androidx.annotation.StringRes
import com.w2sv.domain.R
import kotlinx.coroutines.flow.Flow

sealed interface WidgetWifiProperty : WidgetProperty {
    val descriptionRes: Int
    val learnMoreUrl: String?
    val defaultIsEnabled: Boolean

    sealed interface ViewData {
        val label: String
        val value: String

        data class NonIP(override val value: String, override val label: String) :
            ViewData

        data class IPProperty(
            override val label: String,
            override val value: String,
            val prefixLengthText: String?
        ) : ViewData

        interface Factory {
            /**
             * @return Flow of [ViewData], the element-order of which corresponds to the one of the [properties].
             * One [WidgetWifiProperty] may result in the the creation of multiple [ViewData] elements.
             */
            operator fun invoke(
                properties: Iterable<WidgetWifiProperty>,
                ipSubProperties: Set<IP.SubProperty>
            ): Flow<ViewData>
        }
    }

    sealed interface NonIP : WidgetWifiProperty {

        sealed class LocationAccessRequiring(
            @StringRes override val labelRes: Int,
            @StringRes override val descriptionRes: Int,
            override val learnMoreUrl: String?,
            override val defaultIsEnabled: Boolean
        ) : NonIP {

            data object SSID : LocationAccessRequiring(
                R.string.ssid,
                R.string.ssid_description,
                "https://en.wikipedia.org/wiki/Service_set_(802.11_network)#SSID",
                false
            )

            data object BSSID : LocationAccessRequiring(
                R.string.bssid,
                R.string.bssid_description,
                "https://en.wikipedia.org/wiki/Service_set_(802.11_network)#BSSID",
                false
            )

            companion object {
                val entries: List<LocationAccessRequiring>
                    get() = listOf(SSID, BSSID)
            }
        }

        sealed class Other(
            @StringRes override val labelRes: Int,
            @StringRes override val descriptionRes: Int,
            override val learnMoreUrl: String?,
            override val defaultIsEnabled: Boolean
        ) : NonIP {

            data object Frequency : Other(
                R.string.frequency,
                R.string.frequency_description,
                "https://en.wikipedia.org/wiki/List_of_WLAN_channels",
                true
            )

            data object Channel : Other(
                R.string.channel,
                R.string.channel_description,
                "https://en.wikipedia.org/wiki/List_of_WLAN_channels",
                true
            )

            data object LinkSpeed : Other(
                R.string.link_speed,
                R.string.link_speed_description,
                null,
                true
            )

            data object RSSI : Other(
                R.string.rssi,
                R.string.rssi_description,
                "https://en.wikipedia.org/wiki/Received_signal_strength_indicator",
                true
            )

            data object SignalLevel : Other(
                R.string.signal_level,
                R.string.signal_level_description,
                null,
                true
            )

            data object Standard : Other(
                R.string.standard,
                R.string.standard_description,
                "https://en.wikipedia.org/wiki/IEEE_802.11",
                true
            )

            data object Gateway : Other(
                R.string.gateway,
                R.string.gateway_description,
                "https://en.wikipedia.org/wiki/Gateway_(telecommunications)#Network_gateway",
                true
            )

            data object DNS : Other(
                R.string.dns,
                R.string.dns_description,
                "https://en.wikipedia.org/wiki/Domain_Name_System",
                true
            )

            data object DHCP : Other(
                R.string.dhcp,
                R.string.dhcp_description,
                "https://en.wikipedia.org/wiki/Dynamic_Host_Configuration_Protocol",
                true
            )

            companion object {
                val entries: List<Other>
                    get() = buildList {
                        add(Frequency)
                        add(Channel)
                        add(LinkSpeed)
                        add(RSSI)
                        add(SignalLevel)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                            add(Standard)
                        }
                        add(Gateway)
                        add(DNS)
                        add(DHCP)
                    }
            }
        }
    }

    sealed class IP(subPropertyKinds: List<SubProperty.Kind>) : WidgetWifiProperty {

        abstract val subscriptResId: Int

        val subProperties = subPropertyKinds.map { SubProperty(this, it) }

        val showPrefixLengthSubProperty: SubProperty
            get() = SubProperty(this, SubProperty.Kind.ShowPrefixLength)

        data class SubProperty(val property: IP, val kind: Kind) : WidgetProperty {

            override val labelRes: Int
                get() = kind.labelRes

            sealed class Kind(@StringRes val labelRes: Int) {
                data object ShowPrefixLength : Kind(R.string.show_prefix_length)
            }

            val isAddressTypeEnablementProperty: Boolean
                get() = kind is V4AndV6.AddressTypeEnablement
        }

        sealed class V6Only(
            @StringRes override val labelRes: Int,
            @StringRes override val subscriptResId: Int,
            @StringRes override val descriptionRes: Int,
            override val learnMoreUrl: String?,
            override val defaultIsEnabled: Boolean,
        ) : IP(subPropertyKinds = listOf(SubProperty.Kind.ShowPrefixLength)) {

            data object ULA :
                V6Only(
                    R.string.unique_local_ip,
                    R.string.ula,
                    R.string.unique_local_description,
                    LEARN_MORE_URL,
                    true,
                )

            data object GUA :
                V6Only(
                    R.string.global_unicast_ip,
                    R.string.gua,
                    R.string.global_unicast_description,
                    LEARN_MORE_URL,
                    true,
                )
        }

        sealed class V4AndV6(
            @StringRes override val labelRes: Int,
            @StringRes override val subscriptResId: Int,
            @StringRes override val descriptionRes: Int,
            override val learnMoreUrl: String?,
            override val defaultIsEnabled: Boolean,
            includePrefixLength: Boolean
        ) : IP(
            subPropertyKinds = buildList {
                add(AddressTypeEnablement.V4Enabled)
                add(AddressTypeEnablement.V6Enabled)
                if (includePrefixLength) {
                    add(SubProperty.Kind.ShowPrefixLength)
                }
            }
        ) {
            val v4EnabledSubProperty: SubProperty
                get() = SubProperty(this, AddressTypeEnablement.V4Enabled)

            val v6EnabledSubProperty: SubProperty
                get() = SubProperty(this, AddressTypeEnablement.V6Enabled)

            sealed class AddressTypeEnablement(@StringRes labelRes: Int) :
                SubProperty.Kind(labelRes) {

                abstract val opposingAddressTypeEnablement: AddressTypeEnablement

                data object V4Enabled : AddressTypeEnablement(R.string.ipv4) {
                    override val opposingAddressTypeEnablement: AddressTypeEnablement = V6Enabled
                }

                data object V6Enabled : AddressTypeEnablement(R.string.ipv6) {
                    override val opposingAddressTypeEnablement: AddressTypeEnablement = V4Enabled
                }
            }

            data object Loopback : V4AndV6(
                R.string.loopback_ip,
                R.string.loopback,
                R.string.loopback_description,
                LEARN_MORE_URL,
                true,
                true
            )

            data object SiteLocal :
                V4AndV6(
                    R.string.site_local_ip,
                    R.string.site_local_short,
                    R.string.site_local_description,
                    LEARN_MORE_URL,
                    true,
                    true
                )

            data object LinkLocal : V4AndV6(
                R.string.link_local_ip,
                R.string.link_local_short,
                R.string.link_local_description,
                LEARN_MORE_URL,
                true,
                true
            )

            data object Multicast : V4AndV6(
                R.string.multicast_ip,
                R.string.mc,
                R.string.multicast_description,
                LEARN_MORE_URL,
                true,
                true
            )

            data object Public :
                V4AndV6(
                    R.string.public_ip,
                    R.string.public_,
                    R.string.public_description,
                    LEARN_MORE_URL,
                    true,
                    false
                )
        }

        companion object {
            const val LEARN_MORE_URL = "https://en.wikipedia.org/wiki/IP_address"

            val entries: List<IP>
                get() = listOf(
                    V4AndV6.Loopback,
                    V4AndV6.SiteLocal,
                    V4AndV6.LinkLocal,
                    V6Only.ULA,
                    V4AndV6.Multicast,
                    V6Only.GUA,
                    V4AndV6.Public
                )
        }
    }

    companion object {
        val entries: List<WidgetWifiProperty>
            get() = NonIP.LocationAccessRequiring.entries + IP.entries + NonIP.Other.entries
    }
}
