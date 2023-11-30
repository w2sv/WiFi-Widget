package com.w2sv.domain.model

import androidx.annotation.StringRes
import com.w2sv.domain.R
import kotlinx.coroutines.flow.Flow

sealed interface WidgetWifiProperty {
    val viewData: ViewData
    val defaultIsEnabled: Boolean

    data class ViewData(
        @StringRes val labelRes: Int,
        @StringRes val descriptionRes: Int,
        val learnMoreUrl: String? = null,
    )

    sealed interface ValueViewData {
        val label: String
        val value: String

        data class NonIP(override val value: String, override val label: String) :
            ValueViewData

        data class IPProperty(
            override val label: String,
            override val value: String,
            val prefixLengthText: String?
        ) :
            ValueViewData {

            constructor(
                label: String,
                ipAddress: IPAddress,
                showPrefixLength: Boolean
            ) : this(
                label = label,
                value = ipAddress.hostAddressRepresentation,
                prefixLengthText = if (showPrefixLength) "/${ipAddress.prefixLength}" else null
            )
        }

        interface Factory {
            operator fun invoke(properties: Iterable<WidgetWifiProperty>): Flow<ValueViewData>
        }
    }

    sealed interface SubType : WidgetWifiProperty
    sealed interface NonIP : SubType

    sealed class LocationAccessRequiring(
        override val viewData: ViewData,
        override val defaultIsEnabled: Boolean
    ) : NonIP {

        companion object {
            val entries: List<LocationAccessRequiring>
                get() = listOf(SSID, BSSID)
        }
    }

    sealed class IP(
        override val viewData: ViewData,
        override val defaultIsEnabled: Boolean,
        subPropertyKinds: List<SubProperty.Kind>
    ) :
        SubType {

        val subProperties = subPropertyKinds.map { SubProperty(this, it) }

        val showPrefixLengthSubProperty: SubProperty
            get() = SubProperty(this, SubProperty.Kind.ShowPrefixLength)

        data class SubProperty(val property: IP, val kind: Kind) {
            sealed class Kind(@StringRes val labelRes: Int) {
                data object ShowPrefixLength : Kind(R.string.show_prefix_length)
            }

            val isAddressTypeEnablementProperty: Boolean
                get() = kind is V4AndV6.AddressTypeEnablement
        }

        sealed class V6Only(
            viewData: ViewData,
            defaultIsEnabled: Boolean,
        ) : IP(
            viewData = viewData,
            defaultIsEnabled = defaultIsEnabled,
            subPropertyKinds = listOf(SubProperty.Kind.ShowPrefixLength)
        ) {
            companion object {
                val entries: List<V6Only>
                    get() = listOf(UniqueLocal, GlobalUnicast)
            }
        }

        sealed class V4AndV6(
            viewData: ViewData,
            defaultIsEnabled: Boolean,
        ) : IP(
            viewData = viewData,
            defaultIsEnabled = defaultIsEnabled,
            subPropertyKinds = listOf(
                SubProperty.Kind.ShowPrefixLength,
                AddressTypeEnablement.V4Enabled,
                AddressTypeEnablement.V6Enabled,
            )
        ) {
            val v4EnabledSubProperty: SubProperty
                get() = SubProperty(this, AddressTypeEnablement.V4Enabled)

            val v6EnabledSubProperty: SubProperty
                get() = SubProperty(this, AddressTypeEnablement.V6Enabled)

            sealed class AddressTypeEnablement(@StringRes labelRes: Int) :
                SubProperty.Kind(labelRes) {

                data object V4Enabled : AddressTypeEnablement(R.string.show_v4_addresses)
                data object V6Enabled : AddressTypeEnablement(R.string.show_v6_addresses)

                companion object {
                    val entries: List<AddressTypeEnablement>
                        get() = listOf(V4Enabled, V6Enabled)
                }
            }

            companion object {
                val entries: List<V4AndV6>
                    get() = listOf(LinkLocal, SiteLocal, Public)
            }
        }

        companion object {
            const val LEARN_MORE_URL = "https://en.wikipedia.org/wiki/IP_address"

            val entries: List<IP>
                get() = V6Only.entries + V4AndV6.entries
        }
    }

    sealed class Other(
        override val viewData: ViewData,
        override val defaultIsEnabled: Boolean
    ) :
        NonIP

    data object SSID : LocationAccessRequiring(
        ViewData(
            R.string.ssid,
            R.string.ssid_description,
            "https://en.wikipedia.org/wiki/Service_set_(802.11_network)#SSID",
        ),
        true
    )

    data object BSSID : LocationAccessRequiring(
        ViewData(
            R.string.bssid,
            R.string.bssid_description,
            "https://en.wikipedia.org/wiki/Service_set_(802.11_network)#BSSID",
        ),
        false
    )

    data object LinkLocal : IP.V4AndV6(
        ViewData(
            R.string.link_local,
            R.string.ipv4_description,
            LEARN_MORE_URL,
        ),
        true,
    )

    data object SiteLocal :
        IP.V4AndV6(
            ViewData(
                R.string.site_local,
                R.string.ipv4_description,
                LEARN_MORE_URL,
            ),
            true,
        )

    data object UniqueLocal :
        IP.V6Only(
            ViewData(
                R.string.unique_local,
                R.string.ipv4_description,
                LEARN_MORE_URL,
            ),
            true,
        )

    data object GlobalUnicast :
        IP.V6Only(
            ViewData(
                R.string.global_unicast,
                R.string.ipv4_description,
                LEARN_MORE_URL,
            ),
            true,
        )

    data object Public :
        IP.V4AndV6(
            ViewData(
                R.string.public_,
                R.string.ipv4_description,
                LEARN_MORE_URL,
            ),
            false,
        )

    data object Frequency : Other(
        ViewData(
            R.string.frequency,
            R.string.frequency_description,
            "https://en.wikipedia.org/wiki/List_of_WLAN_channels",
        ),
        true
    )

    data object Channel : Other(
        ViewData(
            R.string.channel,
            R.string.channel_description,
            "https://en.wikipedia.org/wiki/List_of_WLAN_channels",
        ),
        true
    )

    data object LinkSpeed : Other(
        ViewData(
            R.string.link_speed,
            R.string.link_speed_description,
            null,
        ),
        true
    )

    data object Gateway : Other(
        ViewData(
            R.string.gateway,
            R.string.gateway_description,
            "https://en.wikipedia.org/wiki/Gateway_(telecommunications)#Network_gateway",
        ),
        true
    )

    data object DNS : Other(
        ViewData(
            R.string.dns,
            R.string.dns_description,
            "https://en.wikipedia.org/wiki/Domain_Name_System",
        ),
        true
    )

    data object DHCP : Other(
        ViewData(
            R.string.dhcp,
            R.string.dhcp_description,
            "https://en.wikipedia.org/wiki/Dynamic_Host_Configuration_Protocol",
        ),
        true
    )

    companion object {
        val entries: List<WidgetWifiProperty>
            get() {
                return listOf(
                    SSID,
                    BSSID,
                    LinkLocal,
                    SiteLocal,
                    UniqueLocal,
                    GlobalUnicast,
                    Public,
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
