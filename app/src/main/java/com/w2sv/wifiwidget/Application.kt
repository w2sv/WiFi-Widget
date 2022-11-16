package com.w2sv.wifiwidget

import android.app.Application
import com.w2sv.typedpreferences.extensions.appPreferences
import com.w2sv.wifiwidget.preferences.BooleanPreferences
import com.w2sv.wifiwidget.preferences.WidgetPreferences
import timber.log.Timber

class Application: Application() {
    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG)
            Timber.plant(Timber.DebugTree())

        val sharedPreferences = appPreferences()
        BooleanPreferences.initialize(sharedPreferences)
        WidgetPreferences.initialize(sharedPreferences)
    }
}