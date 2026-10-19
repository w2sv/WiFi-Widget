package com.w2sv.networking.wifistatus

import android.content.Context
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.LinkProperties
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.WifiManager
import com.w2sv.common.utils.log
import com.w2sv.domain.networking.WifiStatus
import com.w2sv.domain.networking.WifiStatusSource
import com.w2sv.networking.extensions.hasInternetCapability
import com.w2sv.networking.extensions.hasWifiTransport
import com.w2sv.networking.extensions.isAnyWifiConnected
import com.w2sv.networking.extensions.isInternetValidated
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import slimber.log.d

internal class WifiStatusSourceImpl @Inject constructor(
    private val wifiManager: WifiManager,
    private val connectivityManager: ConnectivityManager,
    @ApplicationContext private val context: Context
) : WifiStatusSource {

    private val networkRequest = NetworkRequest.Builder()
        .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
        .build()

    override val status: Flow<WifiStatus> = callbackFlow {
        // Queue the initial status through the same dispatcher path as callback signals.
        trySend(StatusEvent.Refresh)

        val networkCallback = wifiStatusNetworkCallback(emit = ::trySend)
        val platformWifiStateReceiver = platformWifiStateReceiver { state ->
            when (state) {
                Disabling, Disabled -> trySend(StatusEvent.Disabled)
                Enabling, Enabled -> {
                    if (!connectivityManager.isAnyWifiConnected) {
                        trySend(StatusEvent.NotConnected)
                    }
                }

                else -> Unit
            }
        }

        connectivityManager.registerNetworkCallback(networkRequest, networkCallback)
        context.registerReceiver(platformWifiStateReceiver, IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION))

        awaitClose {
            connectivityManager.unregisterNetworkCallback(networkCallback)
            context.unregisterReceiver(platformWifiStateReceiver)
        }
    }.map { event ->
        // A resumed collector can run on Android's callback stack. Switch dispatchers before
        // querying connectivity.
        withContext(Dispatchers.Default) {
            when (event) {
                StatusEvent.Refresh -> snapshot()
                StatusEvent.Disabled -> WifiStatus.Disabled
                StatusEvent.NotConnected -> WifiStatus.NotConnected
            }
        }
    }
        .conflate()

    override val defaultEgressChanges: Flow<Unit> = callbackFlow {
        // Android sends onAvailable before capabilities, including for the initial network.
        // Wait for onCapabilitiesChanged so an unknown intermediate state cannot refresh twice.
        val initial = connectivityManager.activeNetwork
        val initialInternet = initial
            ?.let(connectivityManager::getNetworkCapabilities)
            ?.hasInternetCapability == true
        val callback = defaultEgressNetworkCallback(initial, initialInternet, emit = { trySend(Unit) })
        connectivityManager.registerDefaultNetworkCallback(callback)
        awaitClose { connectivityManager.unregisterNetworkCallback(callback) }
    }.conflate()

    override fun snapshot(): WifiStatus {
        if (!wifiManager.isWifiEnabled) {
            return WifiStatus.Disabled
        }

        val activeNetwork = connectivityManager.activeNetwork
        val capabilities = activeNetwork?.let(connectivityManager::getNetworkCapabilities)
        val hasWifiTransport = capabilities?.hasWifiTransport == true

        return when {
            hasWifiTransport && capabilities.isInternetValidated -> WifiStatus.Connected
            hasWifiTransport -> WifiStatus.ConnectedNoInternet
            connectivityManager.isAnyWifiConnected -> WifiStatus.ConnectedInactive
            else -> WifiStatus.NotConnected
        }.log {
            "WifiStatus=$it | activeNetwork=$activeNetwork | capabilities=$capabilities"
        }
    }
}

private enum class StatusEvent {
    Refresh,
    Disabled,
    NotConnected
}

private fun wifiStatusNetworkCallback(emit: (StatusEvent) -> Unit): ConnectivityManager.NetworkCallback =
    object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            emit(StatusEvent.Refresh)
        }

        override fun onCapabilitiesChanged(network: Network, capabilities: NetworkCapabilities) {
            if (capabilities.hasWifiTransport) {
                emit(StatusEvent.Refresh)
            }
        }

        override fun onLinkPropertiesChanged(network: Network, linkProperties: LinkProperties) {
            emit(StatusEvent.Refresh)
        }

        override fun onLost(network: Network) {
            // Another Wi-Fi network may already have replaced this one. Re-evaluate current
            // status after the callback instead of reporting an unconditional disconnect.
            emit(StatusEvent.Refresh)
        }
    }

private fun defaultEgressNetworkCallback(
    initial: Network?,
    initialInternet: Boolean,
    emit: () -> Unit
): ConnectivityManager.NetworkCallback {
    val lock = Any()
    var current = initial
    var reported = initial to initialInternet
    return object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            synchronized(lock) { current = network }
        }

        override fun onCapabilitiesChanged(network: Network, capabilities: NetworkCapabilities) {
            synchronized(lock) {
                if (network != current) return@synchronized
                val next = network to capabilities.hasInternetCapability
                if (next != reported) {
                    reported = next
                    d { "Default network capability changed: internet=${next.second}" }
                    emit()
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
                        emit()
                    }
                }
            }
        }
    }
}
