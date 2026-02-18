package com.w2sv.networking.propertyviewdata

import android.net.DhcpInfo
import android.net.LinkProperties
import android.net.wifi.WifiInfo
import com.w2sv.domain.model.networking.IpAddress
import com.w2sv.domain.model.networking.IpApiData

internal data class WifiSnapshot(
    val connectionInfo: WifiInfo,
    val dhcpInfo: DhcpInfo,
    val linkProperties: LinkProperties?,
    val publicIps: Map<IpAddress.Version, IpAddress?>,
    val systemIps: List<IpAddress>,
    val ipApiData: IpApiData?
)
