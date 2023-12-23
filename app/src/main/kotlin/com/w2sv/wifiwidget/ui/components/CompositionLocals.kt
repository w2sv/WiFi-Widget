package com.w2sv.wifiwidget.ui.components

import android.location.LocationManager
import androidx.compose.runtime.staticCompositionLocalOf

val LocalLocationManager =
    staticCompositionLocalOf<LocationManager> { throw IllegalStateException() }