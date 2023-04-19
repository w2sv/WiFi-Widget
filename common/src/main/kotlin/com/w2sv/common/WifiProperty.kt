package com.w2sv.common

import androidx.annotation.ArrayRes
import androidx.annotation.StringRes
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import com.w2sv.common.datastore.DataStoreProperty

enum class WifiProperty(
    @StringRes val labelRes: Int,
    @ArrayRes val infoStringArrayRes: Int,
    override val defaultValue: Boolean = true
) :
    DataStoreProperty<Boolean> {
    SSID(R.string.ssid, R.array.ssid, false),
    IP(R.string.ip, R.array.ip),
    Frequency(R.string.frequency, R.array.frequency),
    Linkspeed(R.string.linkspeed, R.array.linkspeed),
    Gateway(R.string.gateway, R.array.gateway),
    DNS(R.string.dns, R.array.dns),
    DHCP(R.string.dhcp, R.array.dhcp),
    Netmask(R.string.netmask, R.array.netmask);

    override val preferencesKey: Preferences.Key<Boolean> = booleanPreferencesKey(name)
}