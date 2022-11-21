package com.w2sv.wifiwidget.preferences

import android.content.SharedPreferences
import com.w2sv.typedpreferences.descendants.BooleanPreferences
import javax.inject.Inject

class BooleanPreferences(sharedPreferences: SharedPreferences) :
    BooleanPreferences(
        "locationPermissionDialogShown" to false,
        sharedPreferences = sharedPreferences
    ) {
    var locationPermissionDialogShown by this
}