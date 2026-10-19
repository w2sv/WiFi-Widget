package com.w2sv.wifiwidget.ui

import androidx.compose.runtime.staticCompositionLocalOf

val LocalAppVersionName = staticCompositionLocalOf<String> {
    error("App version name not provided")
}
