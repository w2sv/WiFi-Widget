package com.w2sv.wifiwidget

import android.os.Bundle
import androidx.activity.ComponentActivity
import com.w2sv.wifiwidget.preferences.GlobalFlags
import com.w2sv.wifiwidget.preferences.WidgetProperties
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
abstract class ApplicationActivity : ComponentActivity() {

    @Inject
    lateinit var globalFlags: GlobalFlags
    @Inject
    lateinit var widgetProperties: WidgetProperties

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycle.addObserver(globalFlags)
        lifecycle.addObserver(widgetProperties)
    }
}