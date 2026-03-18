package com.w2sv.networking.wifistatus.monitor

import android.content.BroadcastReceiver
import android.net.wifi.WifiManager
import com.w2sv.common.utils.broadcastReceiver
import slimber.log.d

internal fun wifiStateReceiver(onReceivedState: (PlatformWifiState) -> Unit): BroadcastReceiver =
    broadcastReceiver { _, intent ->
        if (intent.action != WifiManager.WIFI_STATE_CHANGED_ACTION) return@broadcastReceiver

        val stateInt = intent.getIntExtra(
            WifiManager.EXTRA_WIFI_STATE,
            WifiManager.WIFI_STATE_UNKNOWN
        )
        val state = PlatformWifiState[stateInt]

        d { "Received WifiState $state" }
        onReceivedState(state)
    }

internal enum class PlatformWifiState(val value: Int) {
    Disabling(WifiManager.WIFI_STATE_DISABLING),
    Disabled(WifiManager.WIFI_STATE_DISABLED),
    Enabling(WifiManager.WIFI_STATE_ENABLING),
    Enabled(WifiManager.WIFI_STATE_ENABLED),
    Unknown(WifiManager.WIFI_STATE_UNKNOWN);

    companion object {
        /** Convert an Int from the broadcast to the corresponding enum */
        operator fun get(value: Int): PlatformWifiState =
            entries.firstOrNull { it.value == value } ?: Unknown
    }
}
