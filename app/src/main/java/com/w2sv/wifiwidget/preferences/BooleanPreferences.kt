package com.w2sv.wifiwidget.preferences

import com.w2sv.typedpreferences.descendants.BooleanPreferences
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BooleanPreferences @Inject constructor() : BooleanPreferences(
    "locationPermissionDialogShown" to false
) {
    var locationPermissionDialogShown by this
}