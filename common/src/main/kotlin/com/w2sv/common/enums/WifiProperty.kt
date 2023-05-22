package com.w2sv.common.enums

import android.net.wifi.WifiManager
import androidx.annotation.ArrayRes
import androidx.annotation.StringRes
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import com.w2sv.common.R
import com.w2sv.common.datastore.DataStoreVariable
import com.w2sv.common.frequencyToChannel
import com.w2sv.common.getNetmask
import com.w2sv.common.textualAddressRepresentation

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
        { textualAddressRepresentation(it.connectionInfo.ipAddress) }
    ),
    Frequency(
        R.string.frequency,
        R.array.frequency,
        { quantityRepresentation(it.connectionInfo.frequency, "MHz") }
    ),
    Channel(
        R.string.channel,
        R.array.channel,
        { frequencyToChannel(it.connectionInfo.frequency).toString() }
    ),
    Linkspeed(
        R.string.linkspeed,
        R.array.linkspeed,
        { quantityRepresentation(it.connectionInfo.linkSpeed, "Mbps") }
    ),
    Gateway(
        R.string.gateway,
        R.array.gateway,
        { textualAddressRepresentation(it.dhcpInfo.gateway) }
    ),
    DNS(
        R.string.dns,
        R.array.dns,
        { textualAddressRepresentation(it.dhcpInfo.dns1) }
    ),
    DHCP(
        R.string.dhcp,
        R.array.dhcp,
        { textualAddressRepresentation(it.dhcpInfo.serverAddress) }
    ),
    Netmask(
        R.string.netmask,
        R.array.netmask,
        { getNetmask() }
    );

    override val preferencesKey: Preferences.Key<Boolean> = booleanPreferencesKey(name)
}

private fun quantityRepresentation(quantity: Number, unit: String): String =
    "$quantity $unit"