package com.w2sv.networking

import android.content.Context
import android.net.ConnectivityManager
import android.net.LinkProperties
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import com.w2sv.androidutils.services.getConnectivityManager
import com.w2sv.androidutils.services.getWifiManager
import com.w2sv.domain.model.WifiStatus
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate
import slimber.log.i
import javax.inject.Inject

class WifiStatusMonitor @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val connectivityManager = context.getConnectivityManager()
    private val wifiManager = context.getWifiManager()

    private val networkRequest = NetworkRequest
        .Builder()
        .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
        .build()

    val wifiPropertiesHaveChanged: Flow<Unit> = callbackFlow {
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: NetworkCapabilities,
            ) {
                if (network == connectivityManager.activeNetwork) {
                    i { "onCapabilitiesChanged.send" }
                    channel.trySend(Unit)
                }
            }

            override fun onLinkPropertiesChanged(network: Network, linkProperties: LinkProperties) {
                if (network == connectivityManager.activeNetwork) {
                    i { "onLinkPropertiesChanged.send" }
                    channel.trySend(Unit)
                }
            }
        }

        connectivityManager.registerNetworkCallback(networkRequest, callback)

        awaitClose {
            connectivityManager.unregisterNetworkCallback(callback)
        }
    }
        .conflate()

    val wifiStatus: Flow<WifiStatus> = callbackFlow {
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                channel.trySend(WifiStatus.Connected)
            }

            override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: NetworkCapabilities,
            ) {
                if (network == connectivityManager.activeNetwork) {
                    i { "onCapabilitiesChanged.send" }
                    channel.trySend(WifiStatus.Connected)
                }
            }

            override fun onLinkPropertiesChanged(network: Network, linkProperties: LinkProperties) {
                if (network == connectivityManager.activeNetwork) {
                    i { "onLinkPropertiesChanged.send" }
                    channel.trySend(WifiStatus.Connected)
                }
            }

            override fun onUnavailable() {
                channel.trySend(getNoConnectionPresentStatus(wifiManager))
            }

            override fun onLost(network: Network) {
                channel.trySend(getNoConnectionPresentStatus(wifiManager))
            }
        }

        connectivityManager.registerNetworkCallback(
            networkRequest,
            callback,
        )

        channel.trySend(getWifiStatus(context))

        awaitClose {
            connectivityManager.unregisterNetworkCallback(callback)
        }
    }
        .conflate()
}
