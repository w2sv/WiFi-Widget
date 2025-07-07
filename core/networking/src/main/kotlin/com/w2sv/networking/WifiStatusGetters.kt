package com.w2sv.networking

import android.net.ConnectivityManager
import android.net.wifi.WifiManager
import com.w2sv.domain.model.WifiStatus
import com.w2sv.networking.extensions.isWifiConnected
import javax.inject.Inject

fun getWifiStatus(wifiManager: WifiManager, connectivityManager: ConnectivityManager): WifiStatus =
    when {
        !wifiManager.isWifiEnabled -> WifiStatus.Disabled
        connectivityManager.isWifiConnected == true -> WifiStatus.Connected
        else -> WifiStatus.Disconnected
    }

internal fun WifiManager.getNoConnectionPresentStatus(): WifiStatus =
    if (isWifiEnabled) WifiStatus.Disconnected else WifiStatus.Disabled

class WifiStatusGetter @Inject constructor(private val wifiManager: WifiManager, private val connectivityManager: ConnectivityManager) {
    operator fun invoke(): WifiStatus =
        getWifiStatus(wifiManager, connectivityManager)
}
