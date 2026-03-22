package com.w2sv.datastore.proto.migration

import com.w2sv.domain.model.wifiproperty.WifiProperty

val WifiProperty.legacyPreferencesKeyName: String
    get() = when (this) {
        WifiProperty.SSID -> "SSID"
        WifiProperty.BSSID -> "BSSID"
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
        WifiProperty.IpGpsLocation -> "IpGpsLocation"
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
