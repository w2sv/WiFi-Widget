package com.w2sv.preferences

import android.content.SharedPreferences
import com.w2sv.androidutils.typedpreferences.BooleanPreferences
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GlobalFlags @Inject constructor(sharedPreferences: SharedPreferences) :
    BooleanPreferences(
        "locationPermissionDialogAnswered" to false,
        sharedPreferences = sharedPreferences
    ) {
    var locationPermissionDialogAnswered by this
}