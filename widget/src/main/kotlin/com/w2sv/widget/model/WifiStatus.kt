package com.w2sv.widget.model

import android.content.Context
import com.w2sv.data.connectivityManager
import com.w2sv.data.isWifiConnected
import com.w2sv.data.linkProperties
import com.w2sv.data.wifiManager
import slimber.log.i

enum class WifiStatus(val isConnected: Boolean) {
    Disabled(false),
    Disconnected(false),
    Connected(true);

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