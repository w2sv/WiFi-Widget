package com.w2sv.wifiwidget.ui.sharedstate.location.access_capability

import androidx.compose.runtime.Stable
import com.w2sv.wifiwidget.ui.sharedstate.location.permission_capability.LocationPermissionCapability

@Stable
class LocationAccessCapabilityImpl(
    private val isGpsEnabledProvider: () -> Boolean,
    private val openSettings: () -> Unit,
    permissionCapability: LocationPermissionCapability
) : LocationAccessCapability,
    LocationPermissionCapability by permissionCapability {

    override val isGpsEnabled: Boolean
        get() = isGpsEnabledProvider()

    override fun openLocationSettings() {
        openSettings()
    }
}
