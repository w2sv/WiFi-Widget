package com.w2sv.wifiwidget.ui

import android.location.LocationManager
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.staticCompositionLocalOf
import com.w2sv.wifiwidget.ui.states.LocationAccessState

val LocalLocationManager =
    staticCompositionLocalOf<LocationManager> { noCompositionLocalProvidedFor("LocalLocationManager") }

val LocalLocationAccessState =
    staticCompositionLocalOf<LocationAccessState> { noCompositionLocalProvidedFor("LocationAccessState") }

val LocalUseDarkTheme = compositionLocalOf<Boolean> { noCompositionLocalProvidedFor("LocalUseDarkTheme") }

val LocalSnackbarHostState = staticCompositionLocalOf { SnackbarHostState() }

fun noCompositionLocalProvidedFor(name: String): Nothing {
    error("$name not provided")
}
