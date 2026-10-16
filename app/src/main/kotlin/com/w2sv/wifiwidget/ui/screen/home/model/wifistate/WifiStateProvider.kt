package com.w2sv.wifiwidget.ui.screen.home.model.wifistate

import kotlinx.coroutines.flow.StateFlow

/**
 * Provides home-screen Wi-Fi state and coordinates remote-data synchronization in response to
 * connectivity and lifecycle changes. Cache reconciliation precedes publication of UI inputs.
 */
interface WifiStateProvider {
    /** Current status and enabled property values, updated for connectivity, configuration, and location access. */
    val wifiState: StateFlow<WifiState>

    /** Requests recomputation of location-dependent property values. */
    fun onLocationAccessChanged()

    /** Revalidates cached remote data and attempts recovery when the home screen enters the foreground. */
    fun onForeground()
}
