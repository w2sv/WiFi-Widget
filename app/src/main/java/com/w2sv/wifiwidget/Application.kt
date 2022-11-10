package com.w2sv.wifiwidget

import android.app.Application
import timber.log.Timber

class Application: Application() {
    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG)
            Timber.plant(Timber.DebugTree())
    }
}