package com.w2sv.wifiwidget.ui.screen.home.model.wifistate

import kotlinx.coroutines.flow.StateFlow

/**
 * Provides home-screen state from reconciled Wi-Fi inputs, published remote data, and changes
 * to location access. Remote-data synchronization and launcher rendering have separate owners.
 */
interface WifiStateProvider {
    /** Current status and enabled property values, updated for connectivity, configuration, and location access. */
    val wifiState: StateFlow<WifiState>

    /** Requests recomputation of location-dependent property values. */
    fun onLocationAccessChanged()
}
