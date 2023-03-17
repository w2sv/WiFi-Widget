package com.w2sv.preferences

import android.content.SharedPreferences
import com.w2sv.common.Theme
import com.w2sv.typedpreferences.IntPreferences
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EnumOrdinals @Inject constructor(sharedPreferences: SharedPreferences) :
    IntPreferences(
        "widgetTheme" to Theme.DeviceDefault.ordinal,
        "inAppTheme" to Theme.DeviceDefault.ordinal,
        sharedPreferences = sharedPreferences
    ) {
    var widgetTheme by this
    var inAppTheme by this
}