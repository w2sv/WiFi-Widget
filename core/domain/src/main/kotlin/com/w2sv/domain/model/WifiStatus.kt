package com.w2sv.domain.model

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.w2sv.core.domain.R

enum class WifiStatus(
    @StringRes val labelRes: Int,
    @DrawableRes val iconRes: Int,
) {
    Disabled(R.string.disabled, R.drawable.ic_wifi_off_24),
    Disconnected(R.string.disconnected, R.drawable.ic_wifi_find_24),
    Connected(R.string.connected, R.drawable.ic_wifi_24)
}
