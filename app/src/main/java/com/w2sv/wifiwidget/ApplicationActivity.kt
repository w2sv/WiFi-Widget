package com.w2sv.wifiwidget

import androidx.activity.ComponentActivity
import com.w2sv.typedpreferences.extensions.appPreferences
import com.w2sv.wifiwidget.preferences.preferenceObjects

abstract class ApplicationActivity : ComponentActivity() {
    /**
     * Write changed preferences.
     *
     * Doing it in [onPause] assures preferences being also written, if app exited via home button
     * and subsequently killed from 'recent' list
     */
    override fun onPause() {
        super.onPause()

        val sharedPreferences = appPreferences()

        preferenceObjects()
            .forEach {
                it.writeChangedValues(sharedPreferences)
            }
    }
}