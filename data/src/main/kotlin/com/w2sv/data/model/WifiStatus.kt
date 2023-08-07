package com.w2sv.data.model

import android.content.Context
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.w2sv.data.R
import com.w2sv.data.networking.connectivityManager
import com.w2sv.data.networking.isWifiConnected
import com.w2sv.data.networking.linkProperties
import com.w2sv.data.networking.wifiManager
import slimber.log.i

enum class WifiStatus(
    val isConnected: Boolean,
    @StringRes val labelRes: Int,
    @DrawableRes val iconRes: Int
) {
    Disabled(false, R.string.disabled, R.drawable.ic_wifi_off_24),
    Disconnected(false, R.string.disconnected, R.drawable.ic_wifi_off_24),
    Connected(true, R.string.connected, R.drawable.ic_wifi_24);

    companion object {
        fun get(context: Context): WifiStatus =
            when (context.wifiManager.isWifiEnabled) {
                false -> Disabled
                true -> {
                    when (context.connectivityManager.isWifiConnected) {
                        true, null -> Connected.also {
                            @Suppress("DEPRECATION") (i {
                                "wifiManager.connectionInfo: ${context.wifiManager.connectionInfo}\n" + "wifiManager.dhcpInfo: ${context.wifiManager.dhcpInfo}\n" + "connectivityManager.linkProperties: ${context.connectivityManager.linkProperties}"
                            })
                        }

                        false -> Disconnected
                    }
                }
            }
    }
}