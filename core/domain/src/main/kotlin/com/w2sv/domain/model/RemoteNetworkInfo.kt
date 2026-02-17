package com.w2sv.domain.model

data class RemoteNetworkInfo(
    val ipApiData: IpApiData?,
    val publicIps: Map<IpAddress.Version, IpAddress?>
)
