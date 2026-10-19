package com.w2sv.wifiwidget.ui.location.capability.access

import androidx.compose.runtime.Stable
import com.w2sv.common.utils.IsLocationEnabled
import com.w2sv.wifiwidget.ui.location.capability.permission.LocationPermissionCapability

@Stable
class LocationAccessCapabilityImpl(
    private val locationEnabledProvider: IsLocationEnabled,
    private val openSettings: () -> Unit,
    permissionCapability: LocationPermissionCapability
) : LocationAccessCapability,
    LocationPermissionCapability by permissionCapability {

    override val isLocationEnabled: Boolean
        get() = locationEnabledProvider()

    override fun openLocationSettings() {
        openSettings()
    }
}
