package com.w2sv.wifiwidget.preferences

import com.w2sv.typedpreferences.TypedPreferences

fun preferenceObjects(): List<TypedPreferences<*>> =
    listOf(WidgetPreferences, BooleanPreferences)