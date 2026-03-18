package com.w2sv.networking.wifistatus.provider

import com.w2sv.domain.model.networking.WifiStatus

interface WifiStatusProvider {
    operator fun invoke(): WifiStatus

    /**
     * @return Either [WifiStatus.NotConnected] or [WifiStatus.Disabled].
     */
    fun onConnectionLost(): WifiStatus
}
