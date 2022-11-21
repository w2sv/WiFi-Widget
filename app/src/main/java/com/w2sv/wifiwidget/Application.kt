package com.w2sv.wifiwidget

import android.app.Application
import com.w2sv.typedpreferences.extensions.appPreferences
import com.w2sv.wifiwidget.preferences.BooleanPreferences
import com.w2sv.wifiwidget.preferences.WidgetPreferences
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import javax.inject.Inject

@HiltAndroidApp
class Application : Application() {
    @Inject
    lateinit var booleanPreferences: BooleanPreferences
    @Inject
    lateinit var widgetPreferences: WidgetPreferences

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG)
            Timber.plant(Timber.DebugTree())

        initializePreferenceObjects()
    }

    private fun initializePreferenceObjects() {
        val sharedPreferences = appPreferences()

        listOf(booleanPreferences, widgetPreferences)
            .forEach { it.initialize(sharedPreferences) }
    }
}