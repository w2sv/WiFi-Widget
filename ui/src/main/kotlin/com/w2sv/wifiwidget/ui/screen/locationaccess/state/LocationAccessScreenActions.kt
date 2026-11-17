package com.w2sv.wifiwidget.ui.screen.locationaccess.state

/** User interactions offered by the Location Access screen. */
internal data class LocationAccessScreenActions(
    val onBack: () -> Unit,
    val onRequestForeground: () -> Unit,
    val onRequestBackground: () -> Unit,
    val onOpenAppSettings: () -> Unit,
    val onOpenLocationSettings: () -> Unit,
    val onContinue: () -> Unit
)
