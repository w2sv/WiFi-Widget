package com.w2sv.widget.ui.state

import com.w2sv.domain.networking.WifiStatus

internal sealed interface WidgetWifiState {
    val status: WifiStatus

    data class Connected(override val status: WifiStatus, val rows: List<WidgetPropertyRow>) : WidgetWifiState {
        init {
            require(status.isConnected)
        }
    }

    data class Disconnected(override val status: WifiStatus) : WidgetWifiState {
        init {
            require(!status.isConnected)
        }
    }
}
