package com.w2sv.domain.networking

/**
 * Network data fetched online for the connected Wi-Fi network.
 */
data class RemoteNetworkData(val ipApiData: IpApiData?, val publicIps: List<IpAddress>) {
    companion object {
        val empty = RemoteNetworkData(null, emptyList())
    }
}
