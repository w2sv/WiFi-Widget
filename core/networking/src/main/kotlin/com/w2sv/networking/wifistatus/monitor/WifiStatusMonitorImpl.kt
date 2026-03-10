package com.w2sv.networking.wifistatus.monitor

import android.net.ConnectivityManager
import android.net.LinkProperties
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import com.w2sv.domain.model.networking.WifiStatus
import com.w2sv.networking.extensions.hasWifiTransport
import com.w2sv.networking.wifistatus.provider.WifiStatusProvider
import javax.inject.Inject
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate

internal class WifiStatusMonitorImpl @Inject constructor(
    private val provideWifiStatus: WifiStatusProvider,
    private val connectivityManager: ConnectivityManager
) : WifiStatusMonitor {

    private val networkRequest = NetworkRequest.Builder()
        .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
        .build()

    override val wifiStatus: Flow<WifiStatus> = callbackFlow {
        // Emit initial state
        trySend(provideWifiStatus())

        val callback = networkCallback(
            computeAndEmitStatus = { trySend(provideWifiStatus()) },
            activeNetwork = { connectivityManager.activeNetwork }
        )

        connectivityManager.registerNetworkCallback(networkRequest, callback)

        awaitClose { connectivityManager.unregisterNetworkCallback(callback) }
    }
        .conflate()
}

private fun networkCallback(computeAndEmitStatus: () -> Unit, activeNetwork: () -> Network?): ConnectivityManager.NetworkCallback =
    object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            // A Wi-Fi network became available
            computeAndEmitStatus()
        }

        override fun onCapabilitiesChanged(network: Network, capabilities: NetworkCapabilities) {
            // Only react if this is actually Wi-Fi
            if (capabilities.hasWifiTransport) {
                computeAndEmitStatus()
            }
        }

        override fun onLinkPropertiesChanged(network: Network, linkProperties: LinkProperties) {
            // Only relevant if this network is currently active
            if (network == activeNetwork()) {
                computeAndEmitStatus()
            }
        }

        override fun onLost(network: Network) {
            // A Wi-Fi network disappeared
            computeAndEmitStatus()
        }
    }
