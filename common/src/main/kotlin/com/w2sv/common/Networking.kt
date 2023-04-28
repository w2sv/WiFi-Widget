package com.w2sv.common

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
fun getNetmask(): String =
    getNetworkPrefixLength()
        ?.let {
            val shift = 0xffffffff shl (32 - it)
            "${((shift and 0xff000000) shr 24) and 0xff}" +
                    ".${((shift and 0x00ff0000) shr 16) and 0xff}" +
                    ".${((shift and 0x0000ff00) shr 8) and 0xff}" +
                    ".${(shift and 0x000000ff) and 0xff}"
        }
        ?: "0.0.0.0"

/**
 * Reference: https://stackoverflow.com/a/29017289/12083276
 */
@RequiresPermission(Manifest.permission.INTERNET)
private fun getNetworkPrefixLength(): Short? {
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

/**
 * Reference: https://stackoverflow.com/a/58646104/12083276
 *
 * @param frequency in MHz
 */
fun frequencyToChannel(frequency: Int): Int =
    when {
        frequency == 2484 -> 14
        frequency < 2484 -> (frequency - 2407) / 5
        frequency in 4910..4980 -> (frequency - 4000) / 5
        frequency < 5925 -> (frequency - 5000) / 5
        frequency == 5935 -> 2
        frequency <= 45000 -> (frequency - 5950) / 5
        frequency in 58320..70200 -> (frequency - 56160) / 2160
        else -> -1
    }
