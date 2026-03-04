package com.w2sv.wifiwidget.ui

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.staticCompositionLocalOf
import com.w2sv.wifiwidget.ui.sharedstate.location.access_capability.LocationAccessCapability
import com.w2sv.wifiwidget.ui.util.snackbar.SnackbarVisibility

val LocalLocationAccessCapability =
    staticCompositionLocalOf<LocationAccessCapability> { noCompositionLocalProvidedFor("LocationAccessCapability") }

val LocalUseDarkTheme = compositionLocalOf<Boolean> { noCompositionLocalProvidedFor("LocalUseDarkTheme") }

val LocalSnackbarVisibility = staticCompositionLocalOf { SnackbarVisibility() }
val LocalSnackbarHostState = staticCompositionLocalOf { SnackbarHostState() }

fun noCompositionLocalProvidedFor(name: String): Nothing {
    error("$name not provided")
}
