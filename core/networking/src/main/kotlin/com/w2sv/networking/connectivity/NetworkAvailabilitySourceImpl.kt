package com.w2sv.networking.connectivity

import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import android.os.PowerManager
import com.w2sv.domain.networking.NetworkAvailabilitySnapshot
import com.w2sv.domain.networking.NetworkAvailabilitySource
import javax.inject.Inject

/**
 * Reads permission-sensitive platform state without registering callbacks. The widget receives
 * one snapshot and decides whether an unresolved property warrants a Battery Saver placeholder.
 */
internal class NetworkAvailabilitySourceImpl @Inject constructor(
    private val powerManager: PowerManager,
    private val connectivityManager: ConnectivityManager,
    private val wifiManager: WifiManager
) : NetworkAvailabilitySource {

    /** Collects default-egress and Wi-Fi-local availability for one widget rendering pass. */
    override fun invoke(): NetworkAvailabilitySnapshot {
        val activeNetwork = connectivityManager.activeNetwork
        val activeNetworkCapabilities = activeNetwork?.let(connectivityManager::getNetworkCapabilities)
        val hasUsableDefaultNetwork = activeNetworkCapabilities?.hasInternetCapability == true
        val wifiNetwork = connectivityManager.wifiNetworkSnapshot(
            wifiEnabled = wifiManager.isWifiEnabled,
            defaultNetwork = activeNetwork,
            defaultCapabilities = activeNetworkCapabilities
        )

        return NetworkAvailabilitySnapshot(
            isBatterySaverEnabled = powerManager.isPowerSaveMode,
            hasUsableDefaultNetwork = hasUsableDefaultNetwork,
            hasLinkProperties = wifiNetwork.linkProperties != null,
            isWifiConnected = wifiNetwork.isConnected,
            defaultNetworkIdentity = activeNetwork?.networkHandle,
            isDirectWifiDefaultNetwork = hasUsableDefaultNetwork && activeNetworkCapabilities.isDirectWifiEgress,
            isVpnDefaultNetwork = hasUsableDefaultNetwork &&
                activeNetworkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN)
        )
    }
}
