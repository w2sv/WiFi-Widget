package com.w2sv.common

import android.Manifest
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.annotation.RequiresPermission
import java.net.InetAddress
import java.net.NetworkInterface
import java.nio.ByteBuffer
import java.nio.ByteOrder

private const val FALLBACK_IP4_ADDRESS = "0.0.0.0"

//fun Int.asFormattedIpAddress(): String =
//    Formatter.formatIpAddress(this)

/**
 * Reference: https://stackoverflow.com/a/52663352/12083276
 */
fun textualAddressRepresentation(address: Int): String =
    InetAddress.getByAddress(
        ByteBuffer
            .allocate(Integer.BYTES)
            .order(ByteOrder.LITTLE_ENDIAN)
            .putInt(address)
            .array()
    )
        .hostAddress ?: FALLBACK_IP4_ADDRESS

/**
 * activeNetwork: null when there is no default network, or when the default network is blocked.
 * getNetworkCapabilities: null if the network is unknown or if the |network| argument is null.
 *
 * Reference: https://stackoverflow.com/questions/3841317/how-do-i-see-if-wi-fi-is-connected-on-android
 */
val ConnectivityManager.isWifiConnected: Boolean?
    get() =
        getNetworkCapabilities(activeNetwork)?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)

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
        ?: FALLBACK_IP4_ADDRESS

/**
 * Reference: https://stackoverflow.com/a/29017289/12083276
 */
@RequiresPermission(Manifest.permission.INTERNET)
private fun getNetworkPrefixLength(): Short? {
    val networkInterfaces = NetworkInterface.getNetworkInterfaces()

    while (networkInterfaces.hasMoreElements()) {
        networkInterfaces.nextElement().run {
            if (!isLoopback) {
                interfaceAddresses.forEach { interfaceAddress ->
                    if (interfaceAddress.broadcast != null) {
                        return interfaceAddress.networkPrefixLength
                    }
                }
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
