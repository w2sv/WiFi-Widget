package com.w2sv.wifiwidget.preferences

import com.w2sv.typedpreferences.descendants.BooleanPreferences

object BooleanPreferences : BooleanPreferences(
    "locationPermissionDialogShown" to false
) {
    var locationPermissionDialogShown by this
}