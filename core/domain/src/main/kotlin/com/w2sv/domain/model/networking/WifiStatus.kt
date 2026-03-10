package com.w2sv.domain.model.networking

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.w2sv.core.common.R

enum class WifiStatus(@StringRes val labelRes: Int, @DrawableRes val iconRes: Int) {
    /** Wi-Fi is turned off. */
    Disabled(R.string.disabled, R.drawable.ic_wifi_off_24),

    /** Wi-Fi is on but not connected to any network. */
    Disconnected(R.string.disconnected, R.drawable.ic_wifi_find_24),

    /** Connected to a Wi-Fi network and active. */
    Connected(R.string.connected, R.drawable.ic_wifi_24),

    /** Connected to a Wi-Fi network, but it is not the active network. */
    ConnectedInactive(R.string.connected_inactive, R.drawable.ic_wifi_connected_inactive_24),

    /** Connected to a Wi-Fi network that has no internet access. */
    ConnectedNoInternet(R.string.connected_no_internet, R.drawable.ic_connected_no_internet);

    val isConnected: Boolean
        get() = this in listOf(Connected, ConnectedInactive, ConnectedNoInternet)
}
