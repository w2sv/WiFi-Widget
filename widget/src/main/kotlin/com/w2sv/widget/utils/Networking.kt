package com.w2sv.widget.utils

import android.Manifest
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.text.format.Formatter
import androidx.annotation.RequiresPermission
import java.net.NetworkInterface

@Suppress("DEPRECATION")
fun Int.asFormattedIpAddress(): String =
    Formatter.formatIpAddress(this)

val ConnectivityManager.isWifiConnected: Boolean
    get() =
        getNetworkCapabilities(activeNetwork)
            ?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true

/**
 * Reference: https://stackoverflow.com/a/33094601/12083276
 */
fun netmask(): String {
    networkPrefixLength()?.let {
        val shift = 0xffffffff shl (32 - it)
        return "${((shift and 0xff000000) shr 24) and 0xff}" +
                ".${((shift and 0x00ff0000) shr 16) and 0xff}" +
                ".${((shift and 0x0000ff00) shr 8) and 0xff}" +
                ".${(shift and 0x000000ff) and 0xff}"
    }
    return "0.0.0.0"
}

/**
 * Reference: https://stackoverflow.com/a/29017289/12083276
 */
@RequiresPermission(Manifest.permission.INTERNET)
private fun networkPrefixLength(): Short? {
    val interfaces = NetworkInterface.getNetworkInterfaces()

    while (interfaces.hasMoreElements()) {
        interfaces.nextElement().run {
            if (!isLoopback)
                interfaceAddresses.forEach { interfaceAddress ->
                    if (interfaceAddress.broadcast != null)
                        return interfaceAddress.networkPrefixLength
                }
        }
    }

    return null
}