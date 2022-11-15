package com.w2sv.wifiwidget.activities

import androidx.activity.ComponentActivity
import com.w2sv.typedpreferences.extensions.appPreferences
import com.w2sv.wifiwidget.preferences.BooleanPreferences

abstract class ApplicationActivity: ComponentActivity(){
    override fun onDestroy() {
        super.onDestroy()

        BooleanPreferences.writeChangedValues(appPreferences())
    }
}