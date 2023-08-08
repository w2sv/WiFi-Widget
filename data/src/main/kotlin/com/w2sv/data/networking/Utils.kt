package com.w2sv.data.networking

import android.content.Context
import android.net.ConnectivityManager
import android.net.LinkProperties
import android.net.wifi.WifiManager
import slimber.log.i

val Context.wifiManager: WifiManager get() = getSystemService(WifiManager::class.java)

val Context.connectivityManager: ConnectivityManager get() = getSystemService(ConnectivityManager::class.java)

val ConnectivityManager.linkProperties: LinkProperties? get() = getLinkProperties(activeNetwork)

fun Context.logConnectionInfo() {
    @Suppress("DEPRECATION")
    i {
        "wifiManager.connectionInfo: ${wifiManager.connectionInfo}\n" +
                "wifiManager.dhcpInfo: ${wifiManager.dhcpInfo}\n" +
                "connectivityManager.linkProperties: ${connectivityManager.linkProperties}"
    }
}