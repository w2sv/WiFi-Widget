package com.w2sv.networking

import android.content.Context
import com.w2sv.androidutils.services.getConnectivityManager
import com.w2sv.androidutils.services.getWifiManager
import slimber.log.i

internal fun Context.logConnectionInfo() {
    val wifiManager = getWifiManager()
    val connectivityManager = getConnectivityManager()
    @Suppress("DEPRECATION")
    i {
        "wifiManager.connectionInfo: ${wifiManager.connectionInfo}\n" +
            "wifiManager.dhcpInfo: ${wifiManager.dhcpInfo}\n" +
            "connectivityManager.linkProperties: ${connectivityManager.linkProperties}"
    }
}
