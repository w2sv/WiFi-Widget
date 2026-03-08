package com.w2sv.domain.model.networking

data class RemoteNetworkInfo(val ipApiData: IpApiData?, val publicIps: Map<IpAddress.Version, IpAddress?>)
