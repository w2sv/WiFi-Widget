package com.w2sv.preferences

import android.content.SharedPreferences
import com.w2sv.typedpreferences.FloatPreferences
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FloatPreferences @Inject constructor(sharedPreferences: SharedPreferences) :
    FloatPreferences(
        "opacity" to 1.0f,
        sharedPreferences = sharedPreferences
    ) {
    var opacity by this
}