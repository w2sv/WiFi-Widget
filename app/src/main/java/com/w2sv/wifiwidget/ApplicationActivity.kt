package com.w2sv.wifiwidget

import androidx.activity.ComponentActivity
import com.w2sv.typedpreferences.extensions.appPreferences
import com.w2sv.wifiwidget.preferences.BooleanPreferences
import com.w2sv.wifiwidget.preferences.WidgetPreferences

abstract class ApplicationActivity: ComponentActivity(){
    override fun onDestroy() {
        super.onDestroy()

        val sharedPreferences = appPreferences()
        BooleanPreferences.writeChangedValues(sharedPreferences)
        WidgetPreferences.writeChangedValues(sharedPreferences)
    }
}