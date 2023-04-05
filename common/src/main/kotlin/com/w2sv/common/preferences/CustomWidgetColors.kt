package com.w2sv.common.preferences

import android.content.SharedPreferences
import android.graphics.Color
import com.w2sv.androidutils.typedpreferences.IntPreferences
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CustomWidgetColors @Inject constructor(sharedPreferences: SharedPreferences) : IntPreferences(
    "Background" to Color.GRAY,
    "Labels" to Color.MAGENTA,
    "Other" to Color.BLACK,
    sharedPreferences = sharedPreferences
)