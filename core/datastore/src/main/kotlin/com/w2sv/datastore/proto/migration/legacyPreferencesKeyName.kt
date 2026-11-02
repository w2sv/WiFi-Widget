package com.w2sv.datastore.proto.migration

import com.w2sv.domain.wifiproperty.WifiProperty

/**
 * The [WifiProperty.entries] as of the preferences-based storage, whose indices the legacy property order refers to.
 * Properties introduced afterward must be excluded here.
 */
internal val legacyWifiPropertyEntries: List<WifiProperty> =
    WifiProperty.entries - WifiProperty.AccessPointName

internal val WifiProperty.legacyPreferencesKeyName: String
    get() = when (this) {
        WifiProperty.SSID -> "SSID"
        WifiProperty.BSSID -> "BSSID"
        WifiProperty.AccessPointName -> "AccessPointName" // Introduced after preferences-based storage; key never written
        WifiProperty.Frequency -> "Frequency"
        WifiProperty.Channel -> "Channel"
        WifiProperty.LinkSpeed -> "LinkSpeed"
        WifiProperty.RSSI -> "RSSI"
        WifiProperty.SignalStrength -> "SignalStrength"
        WifiProperty.Standard -> "Standard"
        WifiProperty.Generation -> "Generation"
        WifiProperty.Security -> "Security"
        WifiProperty.Gateway -> "Gateway"
        WifiProperty.DNS -> "DNS"
        WifiProperty.DHCP -> "DHCP"
        WifiProperty.NAT64Prefix -> "NAT64Prefix"
        WifiProperty.Location -> "Location"
        // Existing preferences use the former property name as their key.
        WifiProperty.IpCoordinates -> "IpGpsLocation"
        WifiProperty.ISP -> "ISP"
        WifiProperty.ASN -> "ASN"
        WifiProperty.ULA -> "ULA"
        WifiProperty.GUA -> "GUA"
        WifiProperty.LoopbackIp -> "Loopback"
        WifiProperty.SiteLocalIp -> "SiteLocal"
        WifiProperty.LinkLocalIp -> "LinkLocal"
        WifiProperty.MulticastIp -> "Multicast"
        WifiProperty.PublicIp -> "Public"
    }
