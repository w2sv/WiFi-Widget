package com.w2sv.networking.wifistatus.monitor

import android.content.BroadcastReceiver
import android.content.Context
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.LinkProperties
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.WifiManager
import com.w2sv.common.utils.broadcastReceiver
import com.w2sv.domain.model.networking.WifiStatus
import com.w2sv.networking.extensions.hasWifiTransport
import com.w2sv.networking.wifistatus.provider.WifiStatusProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate
import slimber.log.d
import javax.inject.Inject

internal class WifiStatusMonitorImpl @Inject constructor(
    private val provideWifiStatus: WifiStatusProvider,
    private val connectivityManager: ConnectivityManager,
    @ApplicationContext private val context: Context
) : WifiStatusMonitor {

    private val networkRequest = NetworkRequest.Builder()
        .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
        .build()

    override val wifiStatus: Flow<WifiStatus> = callbackFlow {
        // Emit initial state
        trySend(provideWifiStatus())

        val networkCallback = networkCallback(
            statusProvider = provideWifiStatus,
            emit = ::trySend,
            activeNetwork = { connectivityManager.activeNetwork }
        )
        val wifiStateReceiver = wifiStateReceiver(::trySend)

        // Register callbacks
        connectivityManager.registerNetworkCallback(networkRequest, networkCallback)
        context.registerReceiver(wifiStateReceiver, IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION))

        awaitClose {
            connectivityManager.unregisterNetworkCallback(networkCallback)
            context.unregisterReceiver(wifiStateReceiver)
        }
    }
        .conflate()
}

private fun networkCallback(
    statusProvider: WifiStatusProvider,
    emit: (WifiStatus) -> Unit,
    activeNetwork: () -> Network?
): ConnectivityManager.NetworkCallback =
    object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            // A Wi-Fi network became available
            d { "onAvailable" }
            emit(statusProvider())
        }

        override fun onCapabilitiesChanged(network: Network, capabilities: NetworkCapabilities) {
            // Only react if this is actually Wi-Fi
            if (network == activeNetwork() && capabilities.hasWifiTransport) {
                d { "onCapabilitiesChanged" }
                emit(statusProvider())
            }
        }

        override fun onLinkPropertiesChanged(network: Network, linkProperties: LinkProperties) {
            // Only relevant if this network is currently active
            if (network == activeNetwork()) {
                d { "onLinkPropertiesChanged" }
                emit(statusProvider())
            }
        }

        override fun onLost(network: Network) {
            // A Wi-Fi network disappeared
            d { "onLost" }
            emit(statusProvider.onConnectionLost())
        }
    }

private fun wifiStateReceiver(emit: (WifiStatus) -> Unit): BroadcastReceiver =
    broadcastReceiver { _, intent ->
        if (intent.action != WifiManager.WIFI_STATE_CHANGED_ACTION) return@broadcastReceiver

        val state = intent.getIntExtra(
            WifiManager.EXTRA_WIFI_STATE,
            WifiManager.WIFI_STATE_UNKNOWN
        )

        if (state in listOf(WifiManager.WIFI_STATE_DISABLED, WifiManager.WIFI_STATE_DISABLING)) {
            d { "Received Wifi disabled broadcast" }
            emit(WifiStatus.Disabled)
        }
    }
