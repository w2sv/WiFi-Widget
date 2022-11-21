package com.w2sv.wifiwidget

import android.os.Bundle
import androidx.activity.ComponentActivity
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
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycle.addObserver(booleanPreferences)
        lifecycle.addObserver(widgetPreferences)
    }
}