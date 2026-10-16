package com.w2sv.networking.wifistatus.monitor

import com.w2sv.domain.model.networking.WifiStatus
import kotlinx.coroutines.flow.Flow

/**
 * Monitors Wi-Fi connectivity and changes to the application's default egress network.
 *
 * [wifiStatus] provides Wi-Fi connectivity updates, while [defaultEgressChanges]
 * signals network changes that may affect remotely fetched data independently
 * of Wi-Fi connectivity.
 */
interface WifiStatusMonitor {

    /**
     * Emits the initial [WifiStatus] on collection and subsequent updates triggered by
     * Wi-Fi state, network availability, capabilities, or link-property changes.
     *
     * Consecutive emissions may contain the same status.
     */
    val wifiStatus: Flow<WifiStatus>

    /**
     * Emits when the default network's identity or INTERNET capability changes,
     * including transitions to or from having no default network.
     *
     * The initial network state serves as the baseline; unchanged initial callbacks
     * do not produce emissions.
     */
    val defaultEgressChanges: Flow<Unit>
}
