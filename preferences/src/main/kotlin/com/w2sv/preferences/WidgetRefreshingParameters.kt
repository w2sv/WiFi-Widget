package com.w2sv.preferences

import android.content.SharedPreferences
import com.w2sv.typedpreferences.BooleanPreferences
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WidgetRefreshingParameters @Inject constructor(sharedPreferences: SharedPreferences): BooleanPreferences(
    "refreshPeriodically" to true,
    "refreshOnBatteryLow" to false,
    sharedPreferences = sharedPreferences
){
    val refreshPeriodically by this
    val refreshOnBatteryLow by this
}