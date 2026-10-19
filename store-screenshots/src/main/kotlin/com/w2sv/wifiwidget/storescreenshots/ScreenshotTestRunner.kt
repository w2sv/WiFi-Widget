package com.w2sv.wifiwidget.storescreenshots

import android.app.Application
import android.content.Context
import androidx.test.runner.AndroidJUnitRunner
import dagger.hilt.android.testing.HiltTestApplication

class ScreenshotTestRunner : AndroidJUnitRunner() {
    override fun newApplication(
        cl: ClassLoader?,
        name: String?,
        context: Context?
    ): Application =
        super.newApplication(cl, HiltTestApplication::class.java.name, context)
}
