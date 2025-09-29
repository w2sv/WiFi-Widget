package com.w2sv.networking

import android.net.ConnectivityManager
import android.net.wifi.WifiManager
import android.os.Build
import com.w2sv.common.utils.log
import com.w2sv.domain.model.WifiStatus
import com.w2sv.networking.extensions.activeNetworkIsWifi
import com.w2sv.networking.extensions.activeNetworkTransportInfo
import com.w2sv.networking.extensions.isAnyWifiConnected
import slimber.log.d
import javax.inject.Inject

@Suppress("DEPRECATION")
fun getWifiStatus(wifiManager: WifiManager, connectivityManager: ConnectivityManager): WifiStatus {
    d {
        "isWifiEnabled=${wifiManager.isWifiEnabled} | " +
            "activeNetworkIsWifi=${connectivityManager.activeNetworkIsWifi} | " +
            "isAnyWifiConnected=${connectivityManager.isAnyWifiConnected} | " +
            "allNetworks=${connectivityManager.allNetworks.toList()} | " +
            "activeNetwork=${connectivityManager.activeNetwork}" +
            "${if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) connectivityManager.activeNetworkTransportInfo else ""}"
    }
    return when {
        !wifiManager.isWifiEnabled -> WifiStatus.Disabled
        connectivityManager.activeNetworkIsWifi -> WifiStatus.Connected
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
