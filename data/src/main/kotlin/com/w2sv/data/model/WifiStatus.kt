package com.w2sv.data.model

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.w2sv.androidutils.services.getConnectivityManager
import com.w2sv.androidutils.services.getWifiManager
import com.w2sv.data.R

enum class WifiStatus(
    @StringRes val labelRes: Int,
    @DrawableRes val iconRes: Int,
) {
    Disabled(R.string.disabled, R.drawable.ic_wifi_off_24),
    Disconnected(R.string.disconnected, R.drawable.ic_wifi_find_24),
    Connected(R.string.connected, R.drawable.ic_wifi_24);

    companion object {
        fun get(context: Context): WifiStatus =
            get(
                wifiManager = context.getWifiManager(),
                connectivityManager = context.getConnectivityManager(),
            )

        fun get(wifiManager: WifiManager, connectivityManager: ConnectivityManager): WifiStatus =
            when {
                !wifiManager.isWifiEnabled -> Disabled
                else -> when (connectivityManager.isWifiConnected) {
                    false -> Disconnected
                    else -> Connected
                }
            }

        fun getNoConnectionPresentStatus(wifiManager: WifiManager): WifiStatus =
            if (wifiManager.isWifiEnabled) Disconnected else Disabled
    }
}

/**
 * activeNetwork: null when there is no default network, or when the default network is blocked.
 * getNetworkCapabilities: null if the network is unknown or if the |network| argument is null.
 *
 * Reference: https://stackoverflow.com/questions/3841317/how-do-i-see-if-wi-fi-is-connected-on-android
 */
private val ConnectivityManager.isWifiConnected: Boolean?
    get() =
        getNetworkCapabilities(activeNetwork)?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
