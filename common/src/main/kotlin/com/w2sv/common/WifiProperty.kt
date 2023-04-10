package com.w2sv.common

import androidx.annotation.StringRes
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import com.w2sv.common.preferences.DataStoreProperty

enum class WifiProperty(@StringRes val labelRes: Int, override val defaultValue: Boolean = true) :
    DataStoreProperty<Boolean> {
    SSID(R.string.ssid, false),
    IP(R.string.ip),
    Frequency(R.string.frequency),
    Linkspeed(R.string.linkspeed),
    Gateway(R.string.gateway),
    DNS(R.string.dns),
    DHCP(R.string.dhcp),
    Netmask(R.string.netmask);

    override val preferencesKey: Preferences.Key<Boolean> = booleanPreferencesKey(name)
}