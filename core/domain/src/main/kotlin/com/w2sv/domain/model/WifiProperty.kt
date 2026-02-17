package com.w2sv.domain.model

import android.os.Build
import androidx.annotation.StringRes
import com.w2sv.core.domain.R

sealed interface WifiProperty : WidgetProperty {

    val descriptionRes: Int
    val learnMoreUrl: String?
    val defaultIsEnabled: Boolean

    val ordinal get() = entries.indexOf(this)

    sealed class NonIP(
        @StringRes override val labelRes: Int,
        @StringRes override val descriptionRes: Int,
        override val learnMoreUrl: String?,
        override val defaultIsEnabled: Boolean = true,
        val requiresLocationAccess: Boolean = false
    ) : WifiProperty {

        data object SSID : NonIP(
            R.string.ssid,
            R.string.ssid_description,
            "https://en.wikipedia.org/wiki/Service_set_(802.11_network)#SSID",
            false,
            true
        )

        data object BSSID : NonIP(
            R.string.bssid,
            R.string.bssid_description,
            "https://en.wikipedia.org/wiki/Service_set_(802.11_network)#BSSID",
            false,
            true
        )

        data object Frequency : NonIP(
            R.string.frequency,
            R.string.frequency_description,
            "https://en.wikipedia.org/wiki/List_of_WLAN_channels"
        )

        data object Channel : NonIP(
            R.string.channel,
            R.string.channel_description,
            "https://en.wikipedia.org/wiki/List_of_WLAN_channels"
        )

        data object LinkSpeed : NonIP(
            R.string.link_speed,
            R.string.link_speed_description,
            null
        )

        data object RSSI : NonIP(
            R.string.rssi,
            R.string.rssi_description,
            "https://en.wikipedia.org/wiki/Received_signal_strength_indicator"
        )

        data object SignalStrength : NonIP(
            R.string.signal_strength,
            R.string.signal_strength_description,
            null
        )

        data object Standard : NonIP(
            R.string.standard,
            R.string.standard_description,
            "https://en.wikipedia.org/wiki/IEEE_802.11"
        )

        data object Generation : NonIP(
            R.string.generation,
            R.string.generation_description,
            "https://en.wikipedia.org/wiki/Wi-Fi_6"
        )

        data object Security : NonIP(
            R.string.security,
            R.string.security_description,
            "https://en.wikipedia.org/wiki/Wi-Fi_Protected_Access"
        )

        data object Gateway : NonIP(
            R.string.gateway,
            R.string.gateway_description,
            "https://en.wikipedia.org/wiki/Gateway_(telecommunications)#Network_gateway"
        )

        data object DNS : NonIP(
            R.string.dns,
            R.string.dns_description,
            "https://en.wikipedia.org/wiki/Domain_Name_System"
        )

        data object DHCP : NonIP(
            R.string.dhcp,
            R.string.dhcp_description,
            "https://en.wikipedia.org/wiki/Dynamic_Host_Configuration_Protocol"
        )

        data object NAT64Prefix : NonIP(
            R.string.nat64prefix,
            R.string.nat64prefix_description,
            "https://en.wikipedia.org/wiki/NAT64"
        )

        data object Location : NonIP(
            R.string.location,
            R.string.location_description,
            "https://en.wikipedia.org/wiki/Internet_geolocation",
            false
        )

        data object IpGpsLocation : NonIP(
            R.string.gps_location,
            R.string.gps_location_description,
            "https://en.wikipedia.org/wiki/Internet_geolocation",
            false
        )

        data object ISP : NonIP(
            R.string.isp,
            R.string.isp_description,
            "https://en.wikipedia.org/wiki/Internet_service_provider",
            false
        )

//            data object AS : Other(
//                R.string.as_name,
//                R.string.as_name_description,
//                "https://en.wikipedia.org/wiki/Autonomous_system_(Internet)",
//                false
//            )

        data object ASN : NonIP(
            R.string.as_number,
            R.string.as_number_description,
            "https://en.wikipedia.org/wiki/Autonomous_system_(Internet)",
            false
        )

        companion object {
            val entries: List<NonIP>
                get() = buildList {
                    add(SSID)
                    add(BSSID)
                    add(Frequency)
                    add(Channel)
                    add(LinkSpeed)
                    add(RSSI)
                    add(SignalStrength)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        add(Standard)
                        add(Generation)
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        add(Security)
                    }
                    add(Gateway)
                    add(DNS)
                    add(DHCP)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        add(NAT64Prefix)
                    }
                    add(Location)
                    add(IpGpsLocation)
                    add(ISP)
//                        add(AS)
                    add(ASN)
                }
        }
    }

    sealed class IP(subPropertyKinds: List<SubProperty.Kind>) : WifiProperty {

        override val learnMoreUrl: String? = "https://en.wikipedia.org/wiki/IP_address"

        @get:StringRes
        abstract val subscriptResId: Int

        val subProperties = subPropertyKinds.map { SubProperty(this, it) }

        val showPrefixLengthSubProperty: SubProperty
            get() = SubProperty(this, SubProperty.Kind.ShowPrefixLength)

        val showSubnetMaskSubProperty: SubProperty
            get() = SubProperty(this, SubProperty.Kind.ShowSubnetMask)

        data class SubProperty(val property: IP, val kind: Kind) : WidgetProperty {

            override val labelRes: Int
                get() = kind.labelRes

            sealed class Kind(@StringRes val labelRes: Int) {
                data object ShowPrefixLength : Kind(R.string.show_prefix_length)
                data object ShowSubnetMask : Kind(R.string.show_ipv4_subnet_mask)
            }

            val isAddressTypeEnablementProperty: Boolean
                get() = kind is V64.AddressTypeEnablement
        }

        sealed class V6Only(
            @StringRes override val labelRes: Int,
            @StringRes override val subscriptResId: Int,
            @StringRes override val descriptionRes: Int,
            override val defaultIsEnabled: Boolean
        ) : IP(subPropertyKinds = listOf(SubProperty.Kind.ShowPrefixLength))

        sealed class V64(
            @StringRes override val labelRes: Int,
            @StringRes override val subscriptResId: Int,
            @StringRes override val descriptionRes: Int,
            override val defaultIsEnabled: Boolean,
            includePrefixLength: Boolean
        ) : IP(
            subPropertyKinds = buildList {
                add(AddressTypeEnablement.V4Enabled)
                add(AddressTypeEnablement.V6Enabled)
                if (includePrefixLength) {
                    add(SubProperty.Kind.ShowPrefixLength)
                    add(SubProperty.Kind.ShowSubnetMask)
                }
            }
        ) {
            val v4EnabledSubProperty: SubProperty
                get() = SubProperty(this, AddressTypeEnablement.V4Enabled)

            val v6EnabledSubProperty: SubProperty
                get() = SubProperty(this, AddressTypeEnablement.V6Enabled)

            sealed class AddressTypeEnablement(@StringRes labelRes: Int) : SubProperty.Kind(labelRes) {

                abstract val opposingAddressTypeEnablement: AddressTypeEnablement

                data object V4Enabled : AddressTypeEnablement(R.string.ipv4) {
                    override val opposingAddressTypeEnablement: AddressTypeEnablement = V6Enabled
                }

                data object V6Enabled : AddressTypeEnablement(R.string.ipv6) {
                    override val opposingAddressTypeEnablement: AddressTypeEnablement = V4Enabled
                }
            }
        }

        data object ULA : V6Only(
            R.string.unique_local_ip,
            R.string.ula,
            R.string.unique_local_description,
            true
        )

        data object GUA : V6Only(
            R.string.global_unicast_ip,
            R.string.gua,
            R.string.global_unicast_description,
            true
        )

        data object Loopback : V64(
            R.string.loopback_ip,
            R.string.loopback,
            R.string.loopback_description,
            true,
            true
        )

        data object SiteLocal : V64(
            R.string.site_local_ip,
            R.string.site_local_short,
            R.string.site_local_description,
            true,
            true
        )

        data object LinkLocal : V64(
            R.string.link_local_ip,
            R.string.link_local_short,
            R.string.link_local_description,
            true,
            true
        )

        data object Multicast : V64(
            R.string.multicast_ip,
            R.string.mc,
            R.string.multicast_description,
            true,
            true
        )

        data object Public : V64(
            R.string.public_ip,
            R.string.public_,
            R.string.public_description,
            true,
            false
        )

        companion object {

            val entries: List<IP> = listOf(
                Loopback,
                SiteLocal,
                LinkLocal,
                ULA,
                Multicast,
                GUA,
                Public
            )
        }
    }

    companion object {
        val entries: List<WifiProperty> = NonIP.entries + IP.entries // TODO ordering (SSID -> IPs -> Other)
        val locationAccessRequiring: List<NonIP> = listOf(NonIP.SSID, NonIP.BSSID)
    }
}
