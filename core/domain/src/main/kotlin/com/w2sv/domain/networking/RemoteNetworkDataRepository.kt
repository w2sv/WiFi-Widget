package com.w2sv.domain.networking

import com.w2sv.domain.networking.RemoteDataConfig
import com.w2sv.domain.networking.RemoteNetworkData
import com.w2sv.domain.networking.RemoteRefreshResult
import kotlinx.coroutines.flow.StateFlow

/**
 * Owns fetching, network-keyed caching, and publication of displayable public-IP and IP-API
 * data against [RemoteDataConfig] requirements. Requirement changes retain only applicable values; a known default-egress change
 * invalidates values from the old network. When the route cannot be identified, cached values
 * remain private until that same network is confirmed again. Synchronous validation shares the
 * cache with concurrent refreshes; callers decide when to synchronize or render widgets.
 */
interface RemoteNetworkDataRepository {

    /** Displayable data; validation may change it without making an HTTP request. */
    val data: StateFlow<RemoteNetworkData>

    /**
     * Refreshes enabled groups and reports whether recovery is satisfied for this egress context.
     * With [onlyIfMissing], fetches only requirements still needing initialization.
     */
    suspend fun refresh(onlyIfMissing: Boolean = false): RemoteRefreshResult

    /** Validates the route and requirements, publishes the resulting [data], and returns it. */
    fun reconciledSnapshot(config: RemoteDataConfig): RemoteNetworkData

    /** Validates and publishes [data], then checks whether an enabled group still needs a fetch. */
    fun needsRecovery(config: RemoteDataConfig): Boolean
}
