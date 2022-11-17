package com.w2sv.wifiwidget

import android.app.Application
import com.w2sv.typedpreferences.extensions.appPreferences
import com.w2sv.wifiwidget.preferences.preferenceObjects
import timber.log.Timber

class Application : Application() {
    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG)
            Timber.plant(Timber.DebugTree())

        initializePreferenceObjects()
    }

    private fun initializePreferenceObjects() {
        val sharedPreferences = appPreferences()

        preferenceObjects()
            .forEach { it.initialize(sharedPreferences) }
    }
}