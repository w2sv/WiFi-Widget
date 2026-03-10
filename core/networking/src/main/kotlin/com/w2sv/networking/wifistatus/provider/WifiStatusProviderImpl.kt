package com.w2sv.networking.wifistatus.provider

import android.net.ConnectivityManager
import android.net.wifi.WifiManager
import com.w2sv.common.utils.log
import com.w2sv.domain.model.networking.WifiStatus
import com.w2sv.networking.extensions.activeNetworkHasInternet
import com.w2sv.networking.extensions.activeNetworkIsWifi
import com.w2sv.networking.extensions.isAnyWifiConnected
import javax.inject.Inject
import slimber.log.d

internal class WifiStatusProviderImpl @Inject constructor(
    private val wifiManager: WifiManager,
    private val connectivityManager: ConnectivityManager
) : WifiStatusProvider {

    override operator fun invoke(): WifiStatus =
        wifiStatus(wifiManager, connectivityManager)
}

private fun wifiStatus(wifiManager: WifiManager, connectivityManager: ConnectivityManager): WifiStatus {
    d {
        "isWifiEnabled=${wifiManager.isWifiEnabled} | " +
            "isWifiConnected=${connectivityManager.activeNetworkIsWifi} | " +
            "isDefaultNetworkActive=${connectivityManager.isDefaultNetworkActive} | " +
            "activeNetwork=${connectivityManager.activeNetwork}"
    }

    return when {
        !wifiManager.isWifiEnabled -> WifiStatus.Disabled
        connectivityManager.activeNetworkIsWifi ->
            if (connectivityManager.activeNetworkHasInternet) {
                WifiStatus.Connected
            } else {
                WifiStatus.ConnectedNoInternet
            }

        connectivityManager.isAnyWifiConnected -> WifiStatus.ConnectedInactive
        else -> WifiStatus.Disconnected
    }
        .log { "Computed WifiStatus.$it" }
}
