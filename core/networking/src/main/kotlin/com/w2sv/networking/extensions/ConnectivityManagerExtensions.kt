package com.w2sv.networking.extensions

import android.net.ConnectivityManager
import android.net.LinkProperties
import android.net.NetworkCapabilities
import com.w2sv.networking.model.IPAddress

internal fun ConnectivityManager.ipAddresses(): List<IPAddress> =
    linkProperties?.linkAddresses?.map { IPAddress(it) } ?: emptyList()

internal val ConnectivityManager.linkProperties: LinkProperties?
    get() = getLinkProperties(activeNetwork)

/**
 * activeNetwork: null when there is no default network, or when the default network is blocked.
 * getNetworkCapabilities: null if the network is unknown or if the |network| argument is null.
 *
 * Reference: https://stackoverflow.com/questions/3841317/how-do-i-see-if-wi-fi-is-connected-on-android
 */
internal val ConnectivityManager.isWifiConnected: Boolean?
    get() =
        getNetworkCapabilities(activeNetwork)?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
