package com.w2sv.networking

import android.net.ConnectivityManager
import android.net.wifi.WifiManager
import com.w2sv.common.utils.log
import com.w2sv.domain.model.WifiStatus
import com.w2sv.networking.extensions.isActiveNetworkWifi
import com.w2sv.networking.extensions.isAnyWifiConnected
import javax.inject.Inject
import slimber.log.d

fun getWifiStatus(wifiManager: WifiManager, connectivityManager: ConnectivityManager): WifiStatus {
    d {
        "isWifiEnabled=${wifiManager.isWifiEnabled} | " +
            "isWifiConnected=${connectivityManager.isActiveNetworkWifi} | " +
            "isDefaultNetworkActive=${connectivityManager.isDefaultNetworkActive} | " +
            "activeNetwork=${connectivityManager.activeNetwork}"
    }
    return when {
        !wifiManager.isWifiEnabled -> WifiStatus.Disabled
        connectivityManager.isActiveNetworkWifi -> WifiStatus.Connected
        connectivityManager.isAnyWifiConnected -> WifiStatus.ConnectedInactive
        else -> WifiStatus.Disconnected
    }
        .log("Determined WifiStatus")
}

internal fun WifiManager.getNoConnectionPresentStatus(): WifiStatus =
    if (isWifiEnabled) WifiStatus.Disconnected else WifiStatus.Disabled

class WifiStatusGetter @Inject constructor(private val wifiManager: WifiManager, private val connectivityManager: ConnectivityManager) {
    operator fun invoke(): WifiStatus =
        getWifiStatus(wifiManager, connectivityManager)
}
