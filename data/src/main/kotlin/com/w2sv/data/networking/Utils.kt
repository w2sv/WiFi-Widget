package com.w2sv.data.networking

import android.content.Context
import android.net.ConnectivityManager
import android.net.LinkProperties
import android.net.wifi.WifiManager
import slimber.log.i

fun Context.getWifiManager(): WifiManager = getSystemService(WifiManager::class.java)

fun Context.getConnectivityManager(): ConnectivityManager =
    getSystemService(ConnectivityManager::class.java)

val ConnectivityManager.linkProperties: LinkProperties? get() = getLinkProperties(activeNetwork)

fun Context.logConnectionInfo() {
    @Suppress("DEPRECATION")
    i {
        "wifiManager.connectionInfo: ${getWifiManager().connectionInfo}\n" +
                "wifiManager.dhcpInfo: ${getWifiManager().dhcpInfo}\n" +
                "connectivityManager.linkProperties: ${getConnectivityManager().linkProperties}"
    }
}