package com.w2sv.ipaddresswidget

import android.app.Application
import timber.log.Timber

class Application: Application() {
    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG)
            Timber.plant(Timber.DebugTree())
    }
}