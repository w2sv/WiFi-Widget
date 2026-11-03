package com.w2sv.domain.networking

/**
 * Data fetched through a verified default HTTP route. Its Wi-Fi association, when one is
 * established, is tracked separately by the repository.
 */
data class RemoteNetworkData(val ipApiData: IpApiData?, val publicIps: List<IpAddress>) {
    companion object {
        val empty = RemoteNetworkData(null, emptyList())
    }
}
