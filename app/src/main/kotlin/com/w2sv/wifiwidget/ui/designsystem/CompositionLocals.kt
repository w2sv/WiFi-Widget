package com.w2sv.wifiwidget.ui.designsystem

import android.annotation.SuppressLint
import android.location.LocationManager
import androidx.compose.runtime.staticCompositionLocalOf

@SuppressLint("ComposeCompositionLocalUsage")
val LocalLocationManager =
    staticCompositionLocalOf<LocationManager> { throw IllegalStateException() }