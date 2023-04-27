package com.w2sv.common.enums

import android.net.wifi.WifiManager
import androidx.annotation.ArrayRes
import androidx.annotation.StringRes
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import com.w2sv.common.R
import com.w2sv.common.asFormattedIpAddress
import com.w2sv.common.datastore.DataStoreVariable
import com.w2sv.common.frequencyToChannel
import com.w2sv.common.getNetmask

@Suppress("DEPRECATION")
enum class WifiProperty(
    @StringRes val labelRes: Int,
    @ArrayRes val infoStringArrayRes: Int,
    val getValue: (WifiManager) -> String,
    override val defaultValue: Boolean = true
) :
    DataStoreVariable<Boolean> {

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
    Channel(
        R.string.channel,
        R.array.channel,
        { frequencyToChannel(it.connectionInfo.frequency).toString() }
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
//    @SuppressLint("HardwareIds")
//    MacAddress(
//        R.string.mac_address,
//        R.array.mac_address,
//        { it.connectionInfo.macAddress }
//    );

    override val preferencesKey: Preferences.Key<Boolean> = booleanPreferencesKey(name)
}