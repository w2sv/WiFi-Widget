package com.w2sv.domain.model.wifiproperty

import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.StringRes
import com.w2sv.core.domain.R
import com.w2sv.domain.model.Labelled
import com.w2sv.domain.model.wifiproperty.settings.IpSetting

sealed class WifiProperty(
    override val protoId: Int,
    @StringRes override val labelRes: Int,
    @StringRes val descriptionRes: Int,
    val learnMoreUrl: String?,
    val isEnabledDefault: Boolean = true,
    val requiresLocationAccess: Boolean = false,
    private val minSdk: Int = Build.VERSION_CODES.BASE
) : WithProtoId, Labelled {

    val isSupported: Boolean
        @SuppressLint("AnnotateVersionCheck", "ObsoleteSdkInt")
        get() = Build.VERSION.SDK_INT >= minSdk

    sealed class IpProperty(
        protoId: Int,
        @StringRes labelRes: Int,
        @StringRes val subscriptResId: Int,
        @StringRes descriptionRes: Int,
        val settings: List<IpSetting>
    ) : WifiProperty(
        protoId = protoId,
        labelRes = labelRes,
        descriptionRes = descriptionRes,
        learnMoreUrl = "https://en.wikipedia.org/wiki/IP_address"
    )

    // -----------------------------
    // Non-IP properties
    // -----------------------------

    data object SSID : WifiProperty(
        protoId = 1,
        labelRes = R.string.ssid,
        descriptionRes = R.string.ssid_description,
        learnMoreUrl = "https://en.wikipedia.org/wiki/Service_set_(802.11_network)#SSID",
        isEnabledDefault = false,
        requiresLocationAccess = true
    )

    data object BSSID : WifiProperty(
        protoId = 2,
        labelRes = R.string.bssid,
        descriptionRes = R.string.bssid_description,
        learnMoreUrl = "https://en.wikipedia.org/wiki/Service_set_(802.11_network)#BSSID",
        isEnabledDefault = false,
        requiresLocationAccess = true
    )

    data object Frequency : WifiProperty(
        protoId = 11,
        labelRes = R.string.frequency,
        descriptionRes = R.string.frequency_description,
        learnMoreUrl = "https://en.wikipedia.org/wiki/List_of_WLAN_channels"
    )

    data object Channel : WifiProperty(
        protoId = 12,
        labelRes = R.string.channel,
        descriptionRes = R.string.channel_description,
        learnMoreUrl = "https://en.wikipedia.org/wiki/List_of_WLAN_channels"
    )

    data object LinkSpeed : WifiProperty(
        protoId = 13,
        labelRes = R.string.link_speed,
        descriptionRes = R.string.link_speed_description,
        learnMoreUrl = null
    )

    data object RSSI : WifiProperty(
        protoId = 14,
        labelRes = R.string.rssi,
        descriptionRes = R.string.rssi_description,
        learnMoreUrl = "https://en.wikipedia.org/wiki/Received_signal_strength_indicator"
    )

    data object SignalStrength : WifiProperty(
        protoId = 15,
        labelRes = R.string.signal_strength,
        descriptionRes = R.string.signal_strength_description,
        learnMoreUrl = null
    )

    data object Standard : WifiProperty(
        protoId = 16,
        labelRes = R.string.standard,
        descriptionRes = R.string.standard_description,
        learnMoreUrl = "https://en.wikipedia.org/wiki/IEEE_802.11",
        minSdk = Build.VERSION_CODES.R
    )

    data object Generation : WifiProperty(
        protoId = 17,
        labelRes = R.string.generation,
        descriptionRes = R.string.generation_description,
        learnMoreUrl = "https://en.wikipedia.org/wiki/Wi-Fi_6",
        minSdk = Build.VERSION_CODES.R
    )

    data object Security : WifiProperty(
        protoId = 18,
        labelRes = R.string.security,
        descriptionRes = R.string.security_description,
        learnMoreUrl = "https://en.wikipedia.org/wiki/Wi-Fi_Protected_Access",
        minSdk = Build.VERSION_CODES.S
    )

    data object Gateway : WifiProperty(
        protoId = 19,
        labelRes = R.string.gateway,
        descriptionRes = R.string.gateway_description,
        learnMoreUrl = "https://en.wikipedia.org/wiki/Gateway_(telecommunications)#Network_gateway"
    )

    data object DNS : WifiProperty(
        protoId = 20,
        labelRes = R.string.dns,
        descriptionRes = R.string.dns_description,
        learnMoreUrl = "https://en.wikipedia.org/wiki/Domain_Name_System"
    )

    data object DHCP : WifiProperty(
        protoId = 21,
        labelRes = R.string.dhcp,
        descriptionRes = R.string.dhcp_description,
        learnMoreUrl = "https://en.wikipedia.org/wiki/Dynamic_Host_Configuration_Protocol"
    )

    data object NAT64Prefix : WifiProperty(
        protoId = 22,
        labelRes = R.string.nat64prefix,
        descriptionRes = R.string.nat64prefix_description,
        learnMoreUrl = "https://en.wikipedia.org/wiki/NAT64",
        minSdk = Build.VERSION_CODES.R
    )

    data object Location : WifiProperty(
        protoId = 23,
        labelRes = R.string.location,
        descriptionRes = R.string.location_description,
        learnMoreUrl = "https://en.wikipedia.org/wiki/Internet_geolocation"
    )

    data object IpGpsLocation : WifiProperty(
        protoId = 24,
        labelRes = R.string.gps_location,
        descriptionRes = R.string.gps_location_description,
        learnMoreUrl = "https://en.wikipedia.org/wiki/Internet_geolocation"
    )

    data object ISP : WifiProperty(
        protoId = 25,
        labelRes = R.string.isp,
        descriptionRes = R.string.isp_description,
        learnMoreUrl = "https://en.wikipedia.org/wiki/Internet_service_provider"
    )

    data object ASN : WifiProperty(
        protoId = 26,
        labelRes = R.string.as_number,
        descriptionRes = R.string.as_number_description,
        learnMoreUrl = "https://en.wikipedia.org/wiki/Autonomous_system_(Internet)"
    )

    // -----------------------------
    // IP properties
    // -----------------------------

    data object ULA : IpProperty(
        protoId = 3,
        labelRes = R.string.unique_local_ip,
        subscriptResId = R.string.ula,
        descriptionRes = R.string.unique_local_description,
        settings = IpSetting.forV6Only
    )

    data object GUA : IpProperty(
        protoId = 4,
        labelRes = R.string.global_unicast_ip,
        subscriptResId = R.string.gua,
        descriptionRes = R.string.global_unicast_description,
        settings = IpSetting.forV6Only
    )

    data object LoopbackIp : IpProperty(
        protoId = 5,
        labelRes = R.string.loopback_ip,
        subscriptResId = R.string.loopback,
        descriptionRes = R.string.loopback_description,
        settings = IpSetting.forV64(true)
    )

    data object SiteLocalIp : IpProperty(
        protoId = 6,
        labelRes = R.string.site_local_ip,
        subscriptResId = R.string.site_local_short,
        descriptionRes = R.string.site_local_description,
        settings = IpSetting.forV64(true)
    )

    data object LinkLocalIp : IpProperty(
        protoId = 7,
        labelRes = R.string.link_local_ip,
        subscriptResId = R.string.link_local_short,
        descriptionRes = R.string.link_local_description,
        settings = IpSetting.forV64(true)
    )

    data object MulticastIp : IpProperty(
        protoId = 8,
        labelRes = R.string.multicast_ip,
        subscriptResId = R.string.mc,
        descriptionRes = R.string.multicast_description,
        settings = IpSetting.forV64(true)
    )

    data object PublicIp : IpProperty(
        protoId = 9,
        labelRes = R.string.public_ip,
        subscriptResId = R.string.public_,
        descriptionRes = R.string.public_description,
        settings = IpSetting.forV64(false)
    )

    companion object {
        val entries = listOf(
            SSID,
            BSSID,
            LoopbackIp,
            SiteLocalIp,
            LinkLocalIp,
            ULA,
            MulticastIp,
            GUA,
            PublicIp,
            Frequency,
            Channel,
            LinkSpeed,
            RSSI,
            SignalStrength,
            Standard,
            Generation,
            Security,
            Gateway,
            DNS,
            DHCP,
            NAT64Prefix,
            Location,
            IpGpsLocation,
            ISP,
            ASN
        )

        val locationAccessRequiring: List<WifiProperty> by lazy {
            entries.filter { it.requiresLocationAccess }
        }
    }
}
