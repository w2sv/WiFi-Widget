package com.w2sv.wifiwidget.preferences

import com.w2sv.typedpreferences.descendants.BooleanPreferences

object BooleanPreferences : BooleanPreferences(
    "locationPermissionDialogAnswered" to false
) {
    var locationPermissionDialogAnswered by this
}