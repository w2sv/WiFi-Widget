package com.w2sv.domain.repository

import com.w2sv.domain.model.RemoteNetworkInfo
import kotlinx.coroutines.flow.StateFlow

/**
 * Provides network-derived information that requires network calls.
 *
 * This includes:
 *  - Public IP addresses (IPv4 / IPv6)
 *  - Location & ISP data via IP APIs (e.g., ip-api.com)
 *
 * Implementations must respect user configuration (enabled properties) to avoid
 * unnecessary network requests.
 */
interface RemoteNetworkInfoRepository {

    val data: StateFlow<RemoteNetworkInfo>

    /**
     * Refreshes all remote network info on demand.
     * It respects user settings and will skip network calls for disabled properties.
     */
    suspend fun refresh()
}
