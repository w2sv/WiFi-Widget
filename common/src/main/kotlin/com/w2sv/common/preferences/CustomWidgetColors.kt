package com.w2sv.common.preferences

import android.content.SharedPreferences
import android.graphics.Color
import com.w2sv.androidutils.typedpreferences.IntPreferences
import com.w2sv.common.WidgetColorSection
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CustomWidgetColors @Inject constructor(sharedPreferences: SharedPreferences) : IntPreferences(
    WidgetColorSection.Background.name to Color.GRAY,
    WidgetColorSection.Labels.name to Color.RED,
    WidgetColorSection.Values.name to Color.BLACK,
    sharedPreferences = sharedPreferences
)