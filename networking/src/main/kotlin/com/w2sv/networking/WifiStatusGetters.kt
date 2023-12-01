package com.w2sv.networking

import android.content.Context
import android.net.ConnectivityManager
import android.net.wifi.WifiManager
import com.w2sv.androidutils.services.getConnectivityManager
import com.w2sv.androidutils.services.getWifiManager
import com.w2sv.domain.model.WifiStatus

fun getWifiStatus(context: Context): WifiStatus =
    getWifiStatus(
        wifiManager = context.getWifiManager(),
        connectivityManager = context.getConnectivityManager(),
    )

fun getWifiStatus(wifiManager: WifiManager, connectivityManager: ConnectivityManager): WifiStatus =
    when {
        !wifiManager.isWifiEnabled -> WifiStatus.Disabled
        else -> when (connectivityManager.isWifiConnected) {
            false -> WifiStatus.Disconnected
            else -> WifiStatus.Connected
        }
    }

internal fun getNoConnectionPresentStatus(wifiManager: WifiManager): WifiStatus =
    if (wifiManager.isWifiEnabled) WifiStatus.Disconnected else WifiStatus.Disabled