package com.w2sv.domain.model.networking

data class RemoteNetworkInfo(val ipApiData: IpApiData?, val publicIps: List<IpAddress>) {
    companion object {
        val empty = RemoteNetworkInfo(null, emptyList())
    }
}
