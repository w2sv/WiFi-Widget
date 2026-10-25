package com.w2sv.domain.networking

/**
 * Captures the network information needed for widget availability and default-route observation.
 * A usable default network has the INTERNET capability; LinkProperties refer to the selected
 * Wi-Fi network and are observed independently because missing remote access does not imply
 * that system IP information is inaccessible. Missing access alone does not identify its cause.
 */
data class NetworkAvailabilitySnapshot(
    val isBatterySaverEnabled: Boolean,
    val hasUsableDefaultNetwork: Boolean,
    val hasLinkProperties: Boolean,
    val isWifiConnected: Boolean,
    /** Identifies the observed default route even before its capabilities become accessible. */
    val defaultNetworkIdentity: Long? = null,
    /** The usable default HTTP route is direct Wi-Fi rather than VPN or mixed cellular transport. */
    val isDirectWifiDefaultNetwork: Boolean = false,
    /** The usable default HTTP route is a VPN, regardless of its underlying transport. */
    val isVpnDefaultNetwork: Boolean = false
) {
    /** Widget HTTP may use only an identifiable Wi-Fi or VPN default while Wi-Fi is connected. */
    val canRefreshWidgetRemoteData: Boolean
        get() = defaultNetworkIdentity != null &&
            isWifiConnected &&
            hasUsableDefaultNetwork &&
            (isDirectWifiDefaultNetwork || isVpnDefaultNetwork)
}
