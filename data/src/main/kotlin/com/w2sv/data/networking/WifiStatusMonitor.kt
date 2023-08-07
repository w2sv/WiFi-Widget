package com.w2sv.data.networking

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkRequest
import com.w2sv.data.model.WifiStatus
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class WifiStatusMonitor @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val callback = object : ConnectivityManager.NetworkCallback() {
        private val scope = CoroutineScope(Dispatchers.Default)

        override fun onUnavailable() {
            super.onUnavailable()

            scope.launch { _wifiStatus.emit(WifiStatus.Disabled) }
        }

        override fun onAvailable(network: Network) {
            scope.launch {
                _wifiStatus.emit(WifiStatus.Connected)
            }
        }

        override fun onLost(network: Network) {
            scope.launch {
                _wifiStatus.emit(if (!context.wifiManager.isWifiEnabled) WifiStatus.Disabled else WifiStatus.Disconnected)
            }
        }
    }

    init {
        registerCallback()
    }

    fun registerCallback() {
        context.connectivityManager.registerNetworkCallback(
            NetworkRequest.Builder().build(),
            callback
        )
    }

    fun unregisterCallback() {
        context.connectivityManager.unregisterNetworkCallback(callback)
    }

    val wifiStatus get() = _wifiStatus.asSharedFlow()
    private val _wifiStatus = MutableSharedFlow<WifiStatus>()
}