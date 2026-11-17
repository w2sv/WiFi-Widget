package com.w2sv.wifiwidget.ui.screen.locationaccess.flow

/** Android operation returned synchronously only when a permission-request event is accepted. */
internal enum class LocationAccessEffect {
    RequestForegroundPermission,
    OpenForegroundSettings,
    LaunchBackgroundPermission
}
