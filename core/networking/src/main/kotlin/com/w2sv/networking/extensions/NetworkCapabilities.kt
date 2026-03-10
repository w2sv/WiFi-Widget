package com.w2sv.networking.extensions

import android.net.NetworkCapabilities

/**
 * Shortcut for `hasTransport(NetworkCapabilities.TRANSPORT_WIFI)`.
 * @see [NetworkCapabilities.hasTransport]
 * @see [NetworkCapabilities.TRANSPORT_WIFI]
 */
internal val NetworkCapabilities.hasWifiTransport: Boolean
    get() = hasTransport(NetworkCapabilities.TRANSPORT_WIFI)

internal val NetworkCapabilities.hasInternet: Boolean
    get() = hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
