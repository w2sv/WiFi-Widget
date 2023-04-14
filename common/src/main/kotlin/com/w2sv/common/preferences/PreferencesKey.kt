package com.w2sv.common.preferences

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey

object PreferencesKey {
    val LOCATION_ACCESS_PERMISSION_RATIONAL_SHOWN =
        booleanPreferencesKey("locationPermissionDialogAnswered")
    val LOCATION_ACCESS_PERMISSION_REQUESTED_AT_LEAST_ONCE =
        booleanPreferencesKey("locationAccessPermissionRequestedAtLeastOnce")
    val OPACITY = floatPreferencesKey("opacity")
    val WIDGET_THEME = intPreferencesKey("widgetTheme")
    val IN_APP_THEME = intPreferencesKey("inAppTheme")
}