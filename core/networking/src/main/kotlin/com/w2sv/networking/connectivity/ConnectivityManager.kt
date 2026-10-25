package com.w2sv.networking.connectivity

import android.net.ConnectivityManager
import android.net.Network

/**
 * @return `true` if the active default network uses Wi-Fi transport.
 * Wi-Fi may still be connected but return `false` here if another network
 * (e.g., mobile data) is the default route.
 *
 * @see ConnectivityManager.getActiveNetwork
 */
internal val ConnectivityManager.activeNetworkIsWifi: Boolean
    get() = activeNetwork?.let(::isWifiConnection) ?: false

/**
 * @return `true` if any connected network uses Wi-Fi transport, regardless of
 * whether it is the active default route for internet traffic.
 */
@Suppress("DEPRECATION")
internal fun ConnectivityManager.isAnyWifiConnected(excludeNetwork: Network? = null): Boolean =
    allNetworks.any { network -> network != excludeNetwork && isWifiConnection(network) }

/**
 * @return `true` if [network] uses Wi-Fi transport.
 */
private fun ConnectivityManager.isWifiConnection(network: Network): Boolean =
    getNetworkCapabilities(network)?.hasWifiTransport == true
