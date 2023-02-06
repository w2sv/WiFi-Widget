package com.w2sv.preferences

import android.content.SharedPreferences
import com.w2sv.typedpreferences.IntPreferences
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class IntPreferences @Inject constructor(sharedPreferences: SharedPreferences) :
    IntPreferences("widgetTheme" to 1, sharedPreferences = sharedPreferences) {

    var widgetTheme by this
}