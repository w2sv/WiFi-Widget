package com.w2sv.networking.remotenetworkinfo

import com.w2sv.domain.model.networking.RemoteDataConfig
import com.w2sv.domain.model.networking.WifiStatus

/**
 * Wi-Fi status and remote requirements, published after the repository has checked cached remote data
 * against the current configuration and default network, and removed values that are no longer valid.
 * Reconciliation does not require an HTTP request.
 */
data class ReconciledWifiInputs(val status: WifiStatus, val remoteDataConfig: RemoteDataConfig)
