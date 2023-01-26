package com.w2sv.wifiwidget.preferences

import android.content.SharedPreferences
import com.w2sv.typedpreferences.descendants.BooleanPreferences
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WidgetProperties @Inject constructor(sharedPreferences: SharedPreferences) :
    BooleanPreferences(
        "SSID" to false,
        "IP" to true,
        "Frequency" to true,
        "Gateway" to true,
        "Netmask" to true,
        "DNS" to true,
        "DHCP" to true,
        sharedPreferences = sharedPreferences
    ) {
    var SSID by this
    var IP by this
    var Frequency by this
    var Gateway by this
    var Netmask by this
    var DNS by this
    var DHCP by this
}