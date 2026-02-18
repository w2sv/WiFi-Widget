package com.w2sv.networking.wifistatus

import android.net.ConnectivityManager
import android.net.wifi.WifiManager
import com.w2sv.domain.model.networking.WifiStatus
import javax.inject.Inject

class WifiStatusGetter @Inject constructor(private val wifiManager: WifiManager, private val connectivityManager: ConnectivityManager) {
    operator fun invoke(): WifiStatus = wifiStatus(wifiManager, connectivityManager)
}
