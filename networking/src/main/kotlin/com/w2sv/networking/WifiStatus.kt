package com.w2sv.networking

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
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

fun getNoConnectionPresentStatus(wifiManager: WifiManager): WifiStatus =
    if (wifiManager.isWifiEnabled) WifiStatus.Disconnected else WifiStatus.Disabled

/**
 * activeNetwork: null when there is no default network, or when the default network is blocked.
 * getNetworkCapabilities: null if the network is unknown or if the |network| argument is null.
 *
 * Reference: https://stackoverflow.com/questions/3841317/how-do-i-see-if-wi-fi-is-connected-on-android
 */
private val ConnectivityManager.isWifiConnected: Boolean?
    get() =
        getNetworkCapabilities(activeNetwork)?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)