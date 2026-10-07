package com.w2sv.wifiwidget.ui.sharedstate.location.capability.access

import androidx.compose.runtime.Stable
import com.w2sv.wifiwidget.ui.sharedstate.location.capability.permission.LocationPermissionCapability

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
