package com.w2sv.common

import android.net.wifi.WifiManager
import androidx.annotation.ArrayRes
import androidx.annotation.StringRes
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import com.w2sv.common.datastore.DataStoreProperty

@Suppress("DEPRECATION")
enum class WifiProperty(
    @StringRes val labelRes: Int,
    @ArrayRes val infoStringArrayRes: Int,
    val getValue: (WifiManager) -> String,
    override val defaultValue: Boolean = true
) :
    DataStoreProperty<Boolean> {

    SSID(
        R.string.ssid,
        R.array.ssid,
        {
            it.connectionInfo.ssid.replace("\"", "")
        },
        false
    ),
    IP(
        R.string.ip,
        R.array.ip,
        { it.connectionInfo.ipAddress.asFormattedIpAddress() }
    ),
    Frequency(
        R.string.frequency,
        R.array.frequency,
        { "${it.connectionInfo.frequency} MHz" }
    ),
    Linkspeed(
        R.string.linkspeed,
        R.array.linkspeed,
        { "${it.connectionInfo.linkSpeed} Mbps" }
    ),
    Gateway(
        R.string.gateway,
        R.array.gateway,
        { it.dhcpInfo.gateway.asFormattedIpAddress() }
    ),
    DNS(
        R.string.dns,
        R.array.dns,
        { it.dhcpInfo.dns1.asFormattedIpAddress() }
    ),
    DHCP(
        R.string.dhcp,
        R.array.dhcp,
        { it.dhcpInfo.serverAddress.asFormattedIpAddress() }
    ),
    Netmask(
        R.string.netmask,
        R.array.netmask,
        { getNetmask() }
    );

    override val preferencesKey: Preferences.Key<Boolean> = booleanPreferencesKey(name)
}