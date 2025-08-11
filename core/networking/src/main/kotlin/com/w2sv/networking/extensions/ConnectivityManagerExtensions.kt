package com.w2sv.networking.extensions

import android.net.ConnectivityManager
import android.net.LinkProperties
import android.net.Network
import android.net.NetworkCapabilities

/**
 * @return the [LinkProperties] for the currently active network, retrieved via [ConnectivityManager.getActiveNetwork] or `null` if:
 * - there is no active network or it is blocked
 * - if the network is unknown
 *
 *  @see ConnectivityManager.getLinkProperties
 */
internal val ConnectivityManager.linkProperties: LinkProperties?
    get() = getLinkProperties(activeNetwork)

/**
 * @return `true` if the active default network uses Wi-Fi transport.
 * Wi-Fi may still be connected but return `false` here if another network
 * (e.g., mobile data) is the default route.
 *
 * @see ConnectivityManager.getActiveNetwork
 */
internal val ConnectivityManager.isActiveNetworkWifi: Boolean
    get() = activeNetwork?.let(::isWifiConnection) ?: false

/**
 * @return `true` if any connected network uses Wi-Fi transport, regardless of
 * whether it is the active default route for internet traffic.
 */
internal val ConnectivityManager.isAnyWifiConnected: Boolean
    @Suppress("DEPRECATION")
    get() = allNetworks.any { isWifiConnection(it) }

/**
 * @return `true` if [network] uses Wi-Fi transport.
 */
private fun ConnectivityManager.isWifiConnection(network: Network): Boolean =
    getNetworkCapabilities(network)?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true

