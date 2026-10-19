package com.w2sv.networking.propertypresentation

import android.net.DhcpInfo
import android.net.LinkProperties
import android.net.wifi.WifiInfo
import com.w2sv.domain.networking.IpAddress
import com.w2sv.domain.networking.IpApiData

internal data class WifiSnapshot(
    val connectionInfo: WifiInfo,
    val dhcpInfo: DhcpInfo,
    val linkProperties: LinkProperties?,
    val publicIps: List<IpAddress>,
    val systemIps: List<IpAddress>,
    val ipApiData: IpApiData?,
    val isLocationEnabled: Boolean
)
