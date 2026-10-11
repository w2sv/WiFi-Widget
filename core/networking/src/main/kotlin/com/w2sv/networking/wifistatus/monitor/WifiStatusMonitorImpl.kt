package com.w2sv.networking.wifistatus.monitor

import android.content.Context
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.LinkProperties
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.WifiManager
import com.w2sv.domain.model.networking.WifiStatus
import com.w2sv.networking.extensions.hasWifiTransport
import com.w2sv.networking.extensions.isAnyWifiConnected
import com.w2sv.networking.wifistatus.provider.WifiStatusProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import slimber.log.d

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
        trySend(StatusSignal(StatusEvent.Refresh))

        val networkCallback = networkCallback(
            emit = ::trySend
        )
        val platformWifiStateReceiver = platformWifiStateReceiver { state ->
            when (state) {
                PlatformWifiState.Disabling, PlatformWifiState.Disabled -> trySend(StatusSignal(StatusEvent.Disabled))
                PlatformWifiState.Enabling, PlatformWifiState.Enabled -> {
                    if (!connectivityManager.isAnyWifiConnected) {
                        trySend(StatusSignal(StatusEvent.NotConnected))
                    }
                }

                else -> Unit
            }
        }

        // Register callbacks
        connectivityManager.registerNetworkCallback(networkRequest, networkCallback)
        context.registerReceiver(platformWifiStateReceiver, IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION))

        awaitClose {
            connectivityManager.unregisterNetworkCallback(networkCallback)
            context.unregisterReceiver(platformWifiStateReceiver)
        }
    }.map { signal ->
        // NetworkCallback methods must not synchronously query ConnectivityManager, including
        // through WifiStatusProvider. Switch dispatchers so a resumed collector cannot run the
        // lookup on Android's callback stack.
        withContext(Dispatchers.Default) {
            when (signal.event) {
                StatusEvent.Refresh -> provideWifiStatus()
                StatusEvent.Disabled -> WifiStatus.Disabled
                StatusEvent.NotConnected -> WifiStatus.NotConnected
                StatusEvent.ActiveWifiChanged -> if (signal.network == connectivityManager.activeNetwork) provideWifiStatus() else null
            }
        }
    }.filterNotNull()
        .conflate()

    override val defaultEgressChanges: Flow<Unit> = callbackFlow {
        // Android sends onAvailable before capabilities, including for the initial network.
        // Wait for onCapabilitiesChanged so an unknown intermediate state cannot refresh twice.
        val initial = connectivityManager.activeNetwork
        val initialInternet = initial?.let(connectivityManager::getNetworkCapabilities)
            ?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
        val lock = Any()
        var current = initial
        var reported = initial to initialInternet
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                synchronized(lock) { current = network }
            }

            override fun onCapabilitiesChanged(network: Network, capabilities: NetworkCapabilities) {
                synchronized(lock) {
                    if (network != current) return@synchronized
                    val next = network to capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                    if (next != reported) {
                        reported = next
                        d { "Default network capability changed: internet=${next.second}" }
                        trySend(Unit)
                    }
                }
            }

            override fun onLost(network: Network) {
                // A lost *current* default is a real transition to no egress. Keep the last
                // reported route separate so a later A -> null -> A is reported as restoration.
                // A late loss for superseded A after B became current must be ignored.
                synchronized(lock) {
                    if (network == current) {
                        current = null
                        if (reported.first != null) {
                            reported = null to false
                            d { "Default network lost" }
                            trySend(Unit)
                        }
                    }
                }
            }
        }
        connectivityManager.registerDefaultNetworkCallback(callback)
        awaitClose { connectivityManager.unregisterNetworkCallback(callback) }
    }.conflate()
}

private enum class StatusEvent { Refresh, Disabled, NotConnected, ActiveWifiChanged }

private data class StatusSignal(val event: StatusEvent, val network: Network? = null)

private fun networkCallback(emit: (StatusSignal) -> Unit): ConnectivityManager.NetworkCallback =
    object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            // A Wi-Fi network became available
            emit(StatusSignal(StatusEvent.Refresh))
        }

        override fun onCapabilitiesChanged(network: Network, capabilities: NetworkCapabilities) {
            // Only react if this is actually Wi-Fi
            if (capabilities.hasWifiTransport) {
                emit(StatusSignal(StatusEvent.ActiveWifiChanged, network))
            }
        }

        override fun onLinkPropertiesChanged(network: Network, linkProperties: LinkProperties) {
            // Active-network filtering happens after the callback returns.
            emit(StatusSignal(StatusEvent.ActiveWifiChanged, network))
        }

        override fun onLost(network: Network) {
            // Another Wi-Fi network may already have replaced this one. Re-evaluate current
            // status after the callback instead of reporting an unconditional disconnect.
            emit(StatusSignal(StatusEvent.Refresh))
        }
    }
