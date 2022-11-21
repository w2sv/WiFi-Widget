package com.w2sv.wifiwidget.preferences

import com.w2sv.typedpreferences.descendants.BooleanPreferences
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WidgetPreferences @Inject constructor() : BooleanPreferences(
    "showSSID" to false,
    "showIPv4" to true,
    "showFrequency" to true,
    "showGateway" to true,
    "showSubnetMask" to true,
    "showDNS" to true,
    "showDHCP" to true
) {
    var showSSID by this
    var showIPv4 by this
    var showFrequency by this
    var showGateway by this
    var showSubnetMask by this
    var showDNS by this
    var showDHCP by this
}