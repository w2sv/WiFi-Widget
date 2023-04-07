package com.w2sv.common.preferences

import android.content.SharedPreferences
import com.w2sv.androidutils.typedpreferences.BooleanPreferences
import com.w2sv.common.WidgetRefreshingParameter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WidgetRefreshingParameters @Inject constructor(sharedPreferences: SharedPreferences) :
    BooleanPreferences(
        WidgetRefreshingParameter.RefreshPeriodically.name to true,
        WidgetRefreshingParameter.RefreshOnBatteryLow.name to false,
        sharedPreferences = sharedPreferences
    ) {
    fun get(parameter: WidgetRefreshingParameter): Boolean =
        getValue(parameter.name)
}