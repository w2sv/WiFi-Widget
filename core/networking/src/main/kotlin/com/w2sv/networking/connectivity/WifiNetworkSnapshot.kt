package com.w2sv.networking.connectivity

import android.net.ConnectivityManager
import android.net.LinkProperties
import android.net.Network
import android.net.NetworkCapabilities

/**
 * Wi-Fi connection evidence and local link information collected for one resolution pass.
 * A connected network can have inaccessible LinkProperties, and multiple visible Wi-Fi networks
 * can establish connectivity without identifying which local addresses belong to Wi-Fi properties.
 */
internal data class WifiNetworkSnapshot(val isConnected: Boolean, val linkProperties: LinkProperties?)

/** Identifies one direct Wi-Fi connection without assuming that it is the default HTTP route. */
internal data class WifiNetworkSelection(val isConnected: Boolean, val network: Network?)

/**
 * Selects a direct Wi-Fi network without treating another default egress as its local interface.
 * A known Wi-Fi default wins; otherwise only one visible Wi-Fi candidate is unambiguous. When
 * default capabilities are unavailable, the default might itself be Wi-Fi, so another candidate
 * cannot safely be selected for local addresses. This identifies local link information, not
 * the current default route for HTTP.
 */
internal fun ConnectivityManager.wifiNetworkSnapshot(
    wifiEnabled: Boolean,
    defaultNetwork: Network?,
    defaultCapabilities: NetworkCapabilities?
): WifiNetworkSnapshot {
    val selection = selectWifiNetwork(wifiEnabled, defaultNetwork, defaultCapabilities)
    return WifiNetworkSnapshot(selection.isConnected, selection.network?.let(::getLinkProperties))
}

/**
 * Returns a network identity only when direct Wi-Fi selection is unambiguous. A visible network
 * is evidence of the connection, not evidence that HTTP currently uses that network.
 */
@Suppress("DEPRECATION")
internal fun ConnectivityManager.selectWifiNetwork(
    wifiEnabled: Boolean,
    defaultNetwork: Network?,
    defaultCapabilities: NetworkCapabilities?
): WifiNetworkSelection {
    if (!wifiEnabled) return WifiNetworkSelection(false, null)

    if (defaultNetwork != null && defaultCapabilities?.isDirectWifi == true) {
        return WifiNetworkSelection(true, defaultNetwork)
    }

    // allNetworks is suitable for this one-shot read; it does not track later network changes.
    val visibleWifiNetworks = allNetworks.asSequence()
        .filterNot { it == defaultNetwork }
        .filter { getNetworkCapabilities(it)?.isDirectWifi == true }
        .take(2)
        .toList()
    val selectedNetwork = visibleWifiNetworks.singleOrNull()
        ?.takeIf { defaultNetwork == null || defaultCapabilities != null }

    return WifiNetworkSelection(
        isConnected = visibleWifiNetworks.isNotEmpty(),
        network = selectedNetwork
    )
}

/** VPN capabilities can include an underlying Wi-Fi transport without being direct Wi-Fi. */
internal val NetworkCapabilities.isDirectWifi: Boolean
    get() = hasWifiTransport && !hasTransport(NetworkCapabilities.TRANSPORT_VPN)

/** Excludes VPN and mixed cellular transports; callers check INTERNET capability separately. */
internal val NetworkCapabilities.isDirectWifiEgress: Boolean
    get() = isDirectWifi && !hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
