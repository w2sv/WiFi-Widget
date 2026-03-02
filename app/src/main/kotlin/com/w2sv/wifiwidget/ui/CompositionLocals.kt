package com.w2sv.wifiwidget.ui

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.staticCompositionLocalOf
import com.w2sv.wifiwidget.ui.sharedstate.location.access_capability.LocationAccessCapability

val LocalLocationAccessCapability =
    staticCompositionLocalOf<LocationAccessCapability> { noCompositionLocalProvidedFor("LocationAccessCapability") }

val LocalUseDarkTheme = compositionLocalOf<Boolean> { noCompositionLocalProvidedFor("LocalUseDarkTheme") }

val LocalSnackbarHostState = staticCompositionLocalOf { SnackbarHostState() }

fun noCompositionLocalProvidedFor(name: String): Nothing {
    error("$name not provided")
}
