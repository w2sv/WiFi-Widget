package com.w2sv.wifiwidget.ui.location.capability.access

import com.w2sv.wifiwidget.ui.location.capability.permission.LocationPermissionCapability

interface LocationAccessCapability : LocationPermissionCapability {
    val isGpsEnabled: Boolean

    /**
     * Opens the systems Location settings screen where the user can enable or disable location services (GPS).
     */
    fun openLocationSettings()
}
