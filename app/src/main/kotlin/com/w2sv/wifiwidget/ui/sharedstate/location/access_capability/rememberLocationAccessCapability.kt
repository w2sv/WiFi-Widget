package com.w2sv.wifiwidget.ui.sharedstate.location.access_capability

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import com.w2sv.common.utils.openLocationSettingsIntent
import com.w2sv.wifiwidget.ui.sharedstate.location.permission_capability.rememberLocationPermissionCapability

@Composable
fun rememberLocationAccessCapability(isGpsEnabled: () -> Boolean): LocationAccessCapability {
    val permissionCapability = rememberLocationPermissionCapability()
    val gpsProviderState = rememberUpdatedState(isGpsEnabled)
    val context = LocalContext.current

    return remember(permissionCapability, context) {
        LocationAccessCapabilityImpl(
            isGpsEnabledProvider = { gpsProviderState.value() },
            openSettings = { context.startActivity(openLocationSettingsIntent) },
            permissionCapability = permissionCapability
        )
    }
}
