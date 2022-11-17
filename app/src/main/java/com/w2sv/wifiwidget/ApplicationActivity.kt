package com.w2sv.wifiwidget

import androidx.activity.ComponentActivity
import com.w2sv.typedpreferences.extensions.appPreferences
import com.w2sv.wifiwidget.preferences.preferenceObjects

abstract class ApplicationActivity : ComponentActivity() {
    override fun onDestroy() {
        super.onDestroy()

        writePreferenceChanges()
    }

    private fun writePreferenceChanges() {
        val sharedPreferences = appPreferences()

        preferenceObjects()
            .forEach {
                it.writeChangedValues(sharedPreferences)
            }
    }
}