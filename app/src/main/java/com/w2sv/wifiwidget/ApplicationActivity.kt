package com.w2sv.wifiwidget

import androidx.activity.ComponentActivity
import com.w2sv.typedpreferences.extensions.appPreferences
import com.w2sv.wifiwidget.preferences.BooleanPreferences
import com.w2sv.wifiwidget.preferences.WidgetPreferences
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
abstract class ApplicationActivity : ComponentActivity() {

    @Inject
    lateinit var booleanPreferences: BooleanPreferences
    @Inject
    lateinit var widgetPreferences: WidgetPreferences

    /**
     * Write changed preferences.
     *
     * Doing it in [onPause] assures preferences being also written, if app exited via home button
     * and subsequently killed from 'recent' list
     */
    override fun onPause() {
        super.onPause()

        val sharedPreferences = appPreferences()

        listOf(booleanPreferences, widgetPreferences)
            .forEach {
                it.writeChangedValues(sharedPreferences)
            }
    }
}