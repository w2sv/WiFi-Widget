package com.w2sv.preferences

import android.content.SharedPreferences
import com.w2sv.typedpreferences.IntPreferences
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WidgetTheme @Inject constructor(sharedPreferences: SharedPreferences) :
    IntPreferences("theme" to 1, sharedPreferences = sharedPreferences) {

    var theme by this
}