package com.w2sv.common

import androidx.annotation.StringRes

enum class WifiProperty(@StringRes val labelRes: Int) {
    SSID(R.string.ssid),
    IP(R.string.ip),
    Frequency(R.string.frequency),
    Linkspeed(R.string.linkspeed),
    Gateway(R.string.gateway),
    DNS(R.string.dns),
    DHCP(R.string.dhcp),
    Netmask(R.string.netmask)
}