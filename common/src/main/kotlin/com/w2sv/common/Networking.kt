package com.w2sv.common

import android.content.Context
import android.net.ConnectivityManager
import android.net.LinkAddress
import android.net.LinkProperties
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import java.net.InetAddress
import java.nio.ByteBuffer
import java.nio.ByteOrder

val Context.wifiManager: WifiManager get() = getSystemService(WifiManager::class.java)

val Context.connectivityManager: ConnectivityManager get() = getSystemService(ConnectivityManager::class.java)

val ConnectivityManager.linkProperties: LinkProperties? get() = getLinkProperties(activeNetwork)

fun ConnectivityManager.findLinkAddress(predicate: (LinkAddress) -> Boolean): LinkAddress? =
    linkProperties?.linkAddresses?.find(predicate)

fun ConnectivityManager.getPublicIPv6Addresses(): List<InetAddress>? =
    linkProperties
        ?.linkAddresses
        ?.filter { it.addressType == AddressType.IPv6 && !it.address.isLinkLocalAddress }
        ?.map { it.address }

val LinkAddress.addressType: AddressType
    get() = if (prefixLength < 64) AddressType.IPv4 else AddressType.IPv6

enum class AddressType {
    IPv4,
    IPv6
}

//fun Int.asFormattedIpAddress(): String =
//    Formatter.formatIpAddress(this)

/**
 * Reference: https://stackoverflow.com/a/52663352/12083276
 */
fun textualAddressRepresentation(address: Int): String? =
    InetAddress.getByAddress(
        ByteBuffer
            .allocate(Integer.BYTES)
            .order(ByteOrder.LITTLE_ENDIAN)
            .putInt(address)
            .array()
    )
        .hostAddress

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
fun toNetmask(networkPrefixLength: Int): String {
    val shift = 0xffffffff shl (32 - networkPrefixLength)
    return "${((shift and 0xff000000) shr 24) and 0xff}" +
            ".${((shift and 0x00ff0000) shr 16) and 0xff}" +
            ".${((shift and 0x0000ff00) shr 8) and 0xff}" +
            ".${(shift and 0x000000ff) and 0xff}"
}

///**
// * Reference: https://stackoverflow.com/a/29017289/12083276
// */
//@RequiresPermission(Manifest.permission.INTERNET)
//private fun getNetworkPrefixLength(): Short? {
//    val networkInterfaces = NetworkInterface.getNetworkInterfaces()
//
//    while (networkInterfaces.hasMoreElements()) {
//        networkInterfaces.nextElement().run {
//            if (!isLoopback) {
//                interfaceAddresses.forEach { interfaceAddress ->
//                    if (interfaceAddress.broadcast != null) {
//                        return interfaceAddress.networkPrefixLength
//                    }
//                }
//            }
//        }
//    }
//
//    return null
//}

/**
 * Reference: https://stackoverflow.com/a/58646104/12083276
 *
 * @param frequency in MHz
 */
fun frequencyToChannel(frequency: Int): Int =
    when {
        frequency <= 0 -> -1
        frequency == 2484 -> 14
        frequency < 2484 -> (frequency - 2407) / 5
        frequency in 4910..4980 -> (frequency - 4000) / 5
        frequency < 5925 -> (frequency - 5000) / 5
        frequency == 5935 -> 2
        frequency <= 45000 -> (frequency - 5950) / 5
        frequency in 58320..70200 -> (frequency - 56160) / 2160
        else -> -1
    }
