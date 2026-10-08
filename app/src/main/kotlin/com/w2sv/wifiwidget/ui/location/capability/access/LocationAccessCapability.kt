package com.w2sv.wifiwidget.ui.location.capability.access

import androidx.compose.runtime.staticCompositionLocalOf
import com.w2sv.wifiwidget.ui.location.capability.permission.LocationPermissionCapability

val LocalLocationAccessCapability = staticCompositionLocalOf<LocationAccessCapability> {
    error("LocationAccessCapability not provided")
}

interface LocationAccessCapability : LocationPermissionCapability {
    val isLocationEnabled: Boolean

    /**
     * Opens the systems Location settings screen where the user can enable or disable location services (GPS).
     */
    fun openLocationSettings()
}
