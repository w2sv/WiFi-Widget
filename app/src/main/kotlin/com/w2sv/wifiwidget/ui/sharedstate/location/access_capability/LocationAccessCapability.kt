package com.w2sv.wifiwidget.ui.sharedstate.location.access_capability

import com.w2sv.wifiwidget.ui.sharedstate.location.permission_capability.LocationPermissionCapability

interface LocationAccessCapability : LocationPermissionCapability {
    val isGpsEnabled: Boolean

    /**
     * Opens the systems Location settings screen where the user can enable or disable location services (GPS).
     */
    fun openLocationSettings()
}
