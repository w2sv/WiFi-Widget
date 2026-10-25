package com.w2sv.domain.networking

import kotlinx.coroutines.flow.StateFlow

/**
 * Owns fetching, network-keyed caching, and publication of displayable public-IP and IP-API
 * data against [RemoteDataConfig] requirements. [data] describes only the verified default HTTP
 * route. A separate in-memory Wi-Fi association lets widgets read applicable last-known values
 * while that exact Wi-Fi connection remains identifiable, without publishing them to foreground
 * consumers or satisfying recovery. Synchronous validation shares cache state with refreshes.
 */
interface RemoteNetworkDataRepository {

    /** Displayable data; validation may change it without making an HTTP request. */
    val data: StateFlow<RemoteNetworkData>

    /**
     * Refreshes enabled groups and reports whether recovery is satisfied for this egress context.
     * With [onlyIfMissing], fetches only requirements still needing initialization.
     */
    suspend fun refresh(onlyIfMissing: Boolean = false): RemoteRefreshResult

    /**
     * Refreshes for a widget only when the route still supports widget presentation at fetch
     * planning time. Foreground callers continue using [refresh] without this route policy.
     */
    suspend fun refreshForWidget(onlyIfMissing: Boolean = false): RemoteRefreshResult

    /** Validates the route and requirements, publishes the resulting [data], and returns it. */
    fun reconciledSnapshot(config: RemoteDataConfig): RemoteNetworkData

    /**
     * Reconciles the verified default route for widget presentation. Current direct Wi-Fi or VPN
     * values take precedence; otherwise last-known values require the same identifiable Wi-Fi
     * connection that produced them. This read performs no HTTP and does not satisfy recovery.
     */
    fun widgetSnapshot(config: RemoteDataConfig): RemoteNetworkData

    /** Validates and publishes [data], then checks whether an enabled group still needs a fetch. */
    fun needsRecovery(config: RemoteDataConfig): Boolean
}
