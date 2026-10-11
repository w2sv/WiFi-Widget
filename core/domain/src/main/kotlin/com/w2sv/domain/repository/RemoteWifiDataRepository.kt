package com.w2sv.domain.repository

import com.w2sv.domain.model.networking.RemoteRefreshResult
import com.w2sv.domain.model.networking.RemoteWifiData
import com.w2sv.domain.model.widget.WidgetConfig
import kotlinx.coroutines.flow.StateFlow

/**
 * Owns public-IP and IP-API data for the current default egress network and enabled properties.
 * A known route change invalidates cached values. When the route cannot be identified, cached
 * values remain private until that same network is confirmed again.
 */
interface RemoteWifiDataRepository {

    /** Displayable data; validation may change it without making an HTTP request. */
    val data: StateFlow<RemoteWifiData>

    /**
     * Refreshes [data] on demand, skipping disabled properties and reporting whether every
     * enabled remote-data group is initialized for the current egress context.
     */
    suspend fun refresh(onlyIfMissing: Boolean = false): RemoteRefreshResult

    /** Validates the route and selection, publishes the resulting [data], and returns it. */
    fun snapshot(config: WidgetConfig): RemoteWifiData

    /** Validates and publishes [data], then checks whether an enabled group still needs a fetch. */
    fun needsRecovery(config: WidgetConfig): Boolean
}
