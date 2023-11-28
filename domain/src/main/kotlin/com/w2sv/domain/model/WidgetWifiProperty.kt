package com.w2sv.domain.model

import androidx.annotation.StringRes
import com.w2sv.domain.R
import kotlinx.coroutines.flow.Flow

sealed class WidgetWifiProperty(
    val viewData: ViewData,
    val defaultIsEnabled: Boolean,
) {
    data class ViewData(
        @StringRes val labelRes: Int,
        @StringRes val descriptionRes: Int,
        val learnMoreUrl: String? = null,
    )

    sealed interface ValueViewData {
        val label: String
        val value: String

        data class RegularProperty(override val value: String, override val label: String) : ValueViewData
        data class IPProperty(override val label: String, override val value: String, val prefixLengthText: String?) :
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

    data object SSID : WidgetWifiProperty(
        ViewData(
            R.string.ssid,
            R.string.ssid_description,
            "https://en.wikipedia.org/wiki/Service_set_(802.11_network)#SSID",
        ),
        true
    )

    data object BSSID : WidgetWifiProperty(
        ViewData(
            R.string.bssid,
            R.string.bssid_description,
            "https://en.wikipedia.org/wiki/Service_set_(802.11_network)#BSSID",
        ),
        false
    )

    sealed class IPProperty(
        viewData: ViewData,
        defaultIsEnabled: Boolean,
        subPropertyKinds: List<SubProperty.Kind>
    ) :
        WidgetWifiProperty(viewData, defaultIsEnabled) {

        val subProperties = subPropertyKinds.map { SubProperty(this, it) }

        data class SubProperty(val property: IPProperty, val kind: Kind) {
            sealed class Kind(@StringRes val labelRes: Int) {
                data object ShowPrefixLength : Kind(R.string.show_prefix_length)
            }

            val isAddressTypeEnablementProperty: Boolean
                get() = kind is V4AndV6.AddressTypeEnablement
        }

        sealed class V6Only(
            viewData: ViewData,
            defaultIsEnabled: Boolean,
        ) : IPProperty(
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
        ) : IPProperty(
            viewData = viewData,
            defaultIsEnabled = defaultIsEnabled,
            subPropertyKinds = listOf(
                SubProperty.Kind.ShowPrefixLength,
                AddressTypeEnablement.V4Enabled,
                AddressTypeEnablement.V6Enabled,
            )
        ) {

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

            val entries: List<IPProperty>
                get() = V6Only.entries + V4AndV6.entries
        }
    }

    data object LinkLocal :
        IPProperty.V4AndV6(
            ViewData(
                R.string.link_local,
                R.string.ipv4_description,
                LEARN_MORE_URL,
            ),
            true,
        )

    data object SiteLocal :
        IPProperty.V4AndV6(
            ViewData(
                R.string.site_local,
                R.string.ipv4_description,
                LEARN_MORE_URL,
            ),
            true,
        )

    data object UniqueLocal :
        IPProperty.V6Only(
            ViewData(
                R.string.unique_local,
                R.string.ipv4_description,
                LEARN_MORE_URL,
            ),
            true,
        )

    data object GlobalUnicast :
        IPProperty.V6Only(
            ViewData(
                R.string.global_unicast,
                R.string.ipv4_description,
                LEARN_MORE_URL,
            ),
            true,
        )

    data object Public :
        IPProperty.V4AndV6(
            ViewData(
                R.string.public_,
                R.string.ipv4_description,
                LEARN_MORE_URL,
            ),
            false,
        )

    data object Frequency : WidgetWifiProperty(
        ViewData(
            R.string.frequency,
            R.string.frequency_description,
            "https://en.wikipedia.org/wiki/List_of_WLAN_channels",
        ),
        true
    )

    data object Channel : WidgetWifiProperty(
        ViewData(
            R.string.channel,
            R.string.channel_description,
            "https://en.wikipedia.org/wiki/List_of_WLAN_channels",
        ),
        true
    )

    data object LinkSpeed : WidgetWifiProperty(
        ViewData(
            R.string.link_speed,
            R.string.link_speed_description,
            null,
        ),
        true
    )

    data object Gateway : WidgetWifiProperty(
        ViewData(
            R.string.gateway,
            R.string.gateway_description,
            "https://en.wikipedia.org/wiki/Gateway_(telecommunications)#Network_gateway",
        ),
        true
    )

    data object DNS : WidgetWifiProperty(
        ViewData(
            R.string.dns,
            R.string.dns_description,
            "https://en.wikipedia.org/wiki/Domain_Name_System",
        ),
        true
    )

    data object DHCP : WidgetWifiProperty(
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
