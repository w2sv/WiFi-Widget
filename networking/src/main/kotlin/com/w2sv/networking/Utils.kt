package com.w2sv.data.networking

import android.content.Context
import android.net.ConnectivityManager
import android.net.LinkProperties
import com.w2sv.androidutils.services.getConnectivityManager
import com.w2sv.androidutils.services.getWifiManager
import slimber.log.i

val ConnectivityManager.linkProperties: LinkProperties?
    get() = getLinkProperties(activeNetwork)

fun Context.logConnectionInfo() {
    val wifiManager = getWifiManager()
    val connectivityManager = getConnectivityManager()
    @Suppress("DEPRECATION")
    i {
        "wifiManager.connectionInfo: ${wifiManager.connectionInfo}\n" +
            "wifiManager.dhcpInfo: ${wifiManager.dhcpInfo}\n" +
            "connectivityManager.linkProperties: ${connectivityManager.linkProperties}"
    }
}
