package com.w2sv.common.preferences

import android.content.SharedPreferences
import com.w2sv.androidutils.typedpreferences.BooleanPreferences
import com.w2sv.common.WifiProperty
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WidgetProperties @Inject constructor(sharedPreferences: SharedPreferences) :
    BooleanPreferences(
        WifiProperty.SSID.name to false,
        WifiProperty.IP.name to true,
        WifiProperty.Frequency.name to true,
        WifiProperty.Linkspeed.name to true,
        WifiProperty.Gateway.name to true,
        WifiProperty.DNS.name to true,
        WifiProperty.DHCP.name to true,
        WifiProperty.Netmask.name to true,
        sharedPreferences = sharedPreferences
    ){
        fun get(property: WifiProperty): Boolean =
            getValue(property.name)
    }