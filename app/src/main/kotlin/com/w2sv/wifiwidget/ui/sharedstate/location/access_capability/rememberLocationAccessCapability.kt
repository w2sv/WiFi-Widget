package com.w2sv.wifiwidget.ui.sharedstate.location.access_capability

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import com.w2sv.wifiwidget.ui.sharedstate.location.permission_capability.rememberLocationPermissionCapability

@Composable
fun rememberLocationAccessCapability(isGpsEnabled: () -> Boolean): LocationAccessCapability {
    val permissionCapability = rememberLocationPermissionCapability()
    val gpsProviderState = rememberUpdatedState(isGpsEnabled)

    return remember(permissionCapability, isGpsEnabled) {
        LocationAccessCapabilityImpl(
            isGpsEnabledProvider = { gpsProviderState.value() },
            permissionCapability = permissionCapability
        )
    }
}
