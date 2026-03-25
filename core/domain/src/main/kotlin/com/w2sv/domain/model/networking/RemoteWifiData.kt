package com.w2sv.domain.model.networking

/**
 * All the Wifi data that is fetched online.
 */
data class RemoteWifiData(val ipApiData: IpApiData?, val publicIps: List<IpAddress>) {
    companion object {
        val empty = RemoteWifiData(null, emptyList())
    }
}
