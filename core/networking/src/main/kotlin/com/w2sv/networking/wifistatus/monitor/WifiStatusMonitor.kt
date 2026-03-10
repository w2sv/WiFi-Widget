package com.w2sv.networking.wifistatus.monitor

import com.w2sv.domain.model.networking.WifiStatus
import kotlinx.coroutines.flow.Flow

/**
 * Provides a [Flow] of Wi-Fi connectivity status updates.
 *
 * Emits a new [WifiStatus] on:
 * - initial evaluation on subscription
 * - Wi-Fi enablement or disablement
 * - changes to the active network or link properties
 * - availability or loss of Wi-Fi networks
 *
 * Internally driven by [android.net.ConnectivityManager.NetworkCallback] and the current [android.net.wifi.WifiManager] state.
 */
interface WifiStatusMonitor {
    val wifiStatus: Flow<WifiStatus>
}
