package com.w2sv.networking.wifistatus

import android.content.Context
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.LinkProperties
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.WifiManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.w2sv.domain.networking.WifiStatus
import com.w2sv.domain.networking.WifiStatusSource
import com.w2sv.networking.connectivity.hasInternetCapability
import com.w2sv.networking.connectivity.hasWifiTransport
import com.w2sv.networking.connectivity.isAnyWifiConnected
import com.w2sv.networking.connectivity.isInternetValidated
import com.w2sv.networking.connectivity.selectWifiNetwork
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
        trySend(Unit)

        val networkCallback = wifiStatusNetworkCallback { trySend(Unit) }
        connectivityManager.registerNetworkCallback(networkRequest, networkCallback)

        val unregisterAdapterStateObservation = try {
            registerAdapterStateObservation { trySend(Unit) }
        } catch (registrationFailure: Exception) {
            try {
                connectivityManager.unregisterNetworkCallback(networkCallback)
            } catch (cleanupFailure: Throwable) {
                if (cleanupFailure !== registrationFailure) registrationFailure.addSuppressed(cleanupFailure)
            }
            throw registrationFailure
        }

        awaitClose {
            try {
                connectivityManager.unregisterNetworkCallback(networkCallback)
            } finally {
                unregisterAdapterStateObservation()
            }
        }
    }.map {
        // A resumed collector can run on Android's callback stack. Switch dispatchers before
        // querying connectivity.
        withContext(Dispatchers.Default) { snapshot() }
    }
        .conflate()

    private fun registerAdapterStateObservation(onChanged: () -> Unit): () -> Unit {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.BAKLAVA) {
            val listener = WifiManager.WifiStateChangedListener {
                d { "Wi-Fi adapter state observed: ${PlatformWifiState[wifiManager.wifiState]}" }
                onChanged()
            }
            wifiManager.addWifiStateChangedListener(context.mainExecutor, listener)
            return { wifiManager.removeWifiStateChangedListener(listener) }
        }

        val receiver = platformWifiStateReceiver { state ->
            if (state != Unknown) onChanged()
        }
        ContextCompat.registerReceiver(
            context,
            receiver,
            IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION),
            ContextCompat.RECEIVER_EXPORTED
        )
        return { context.unregisterReceiver(receiver) }
    }

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
        val status = if (!wifiManager.isWifiEnabled) {
            WifiStatus.Disabled
        } else {
            val activeNetwork = connectivityManager.activeNetwork
            val activeCapabilities = activeNetwork?.let(connectivityManager::getNetworkCapabilities)
            val activeHasWifiTransport = activeCapabilities?.hasWifiTransport == true

            when {
                activeHasWifiTransport && activeCapabilities.isInternetValidated -> WifiStatus.Connected
                activeHasWifiTransport -> WifiStatus.ConnectedNoInternet
                connectivityManager.isAnyWifiConnected(activeNetwork) -> WifiStatus.ConnectedInactive
                else -> WifiStatus.NotConnected
            }
        }
        d { "Wi-Fi status: adapter=${PlatformWifiState[wifiManager.wifiState]}, status=$status" }
        return status
    }

    /** Uses the same direct Wi-Fi selection as local property resolution and widget cache reads. */
    override fun connectedWifiIdentity(): Long? {
        val defaultNetwork = connectivityManager.activeNetwork
        val capabilities = defaultNetwork?.let(connectivityManager::getNetworkCapabilities)
        return connectivityManager.selectWifiNetwork(
            wifiManager.isWifiEnabled,
            defaultNetwork,
            capabilities
        ).network?.networkHandle
    }
}

private fun wifiStatusNetworkCallback(onChanged: () -> Unit): ConnectivityManager.NetworkCallback =
    object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            onChanged()
        }

        override fun onCapabilitiesChanged(network: Network, capabilities: NetworkCapabilities) {
            if (capabilities.hasWifiTransport) {
                onChanged()
            }
        }

        override fun onLinkPropertiesChanged(network: Network, linkProperties: LinkProperties) {
            onChanged()
        }

        override fun onLost(network: Network) {
            d { "Wi-Fi network lost: $network" }
            // Another Wi-Fi network may already have replaced this one. Re-evaluate current
            // status after the callback instead of reporting an unconditional disconnect.
            onChanged()
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
