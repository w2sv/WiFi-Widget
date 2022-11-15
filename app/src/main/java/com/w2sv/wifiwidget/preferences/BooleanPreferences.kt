package com.w2sv.wifiwidget.preferences

import com.w2sv.typedpreferences.descendants.BooleanPreferences
import com.w2sv.wifiwidget.R

object BooleanPreferences : BooleanPreferences(
    "showSSID" to true,
    "showIPv4" to true,
    "showFrequency" to true,
    "showGateway" to true,
    "showSubnetMask" to true,
    "showDNS" to true,
    "showDHCP" to true,
    "locationPermissionDialogShown" to false
) {
    var showSSID by this
    var showIPv4 by this
    var showFrequency by this
    var showGateway by this
    var showSubnetMask by this
    var showDNS by this
    var showDHCP by this

    var locationPermissionDialogShown by this
}
