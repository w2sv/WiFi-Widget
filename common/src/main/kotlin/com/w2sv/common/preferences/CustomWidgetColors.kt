package com.w2sv.common.preferences

import android.content.SharedPreferences
import android.graphics.Color
import com.w2sv.androidutils.typedpreferences.IntPreferences
import com.w2sv.common.CustomizableWidgetSection
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CustomWidgetColors @Inject constructor(sharedPreferences: SharedPreferences) : IntPreferences(
    CustomizableWidgetSection.Background.name to Color.GRAY,
    CustomizableWidgetSection.Labels.name to Color.RED,
    CustomizableWidgetSection.Values.name to Color.BLACK,
    sharedPreferences = sharedPreferences
)