package com.w2sv.wifiwidget.ui.screen.home.synchronization

import com.w2sv.domain.networking.RemoteDataConfig
import com.w2sv.domain.networking.WifiStatus

/**
 * Wi-Fi status and remote requirements, published after the repository has checked cached remote data
 * against the current configuration and default network, and removed values that are no longer valid.
 * Reconciliation does not require an HTTP request.
 */
data class ReconciledWifiInputs(val status: WifiStatus, val remoteDataConfig: RemoteDataConfig)
