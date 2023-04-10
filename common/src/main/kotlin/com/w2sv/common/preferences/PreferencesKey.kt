package com.w2sv.common.preferences

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey

object PreferencesKey {
    val LOCATION_PERMISSION_DIALOG_ANSWERED =
        booleanPreferencesKey("locationPermissionDialogAnswered")
    val OPACITY = floatPreferencesKey("opacity")
    val WIDGET_THEME = intPreferencesKey("widgetTheme")
    val IN_APP_THEME = intPreferencesKey("inAppTheme")
}