package com.w2sv.wifiwidget.ui.location.capability.access

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.w2sv.augmentedpermissions.PermissionRequestHistory
import com.w2sv.common.utils.IsLocationEnabled
import com.w2sv.common.utils.openLocationSettingsIntent
import com.w2sv.wifiwidget.ui.location.LocationAccessRationalHistory
import com.w2sv.wifiwidget.ui.location.capability.permission.rememberLocationPermissionCapability

@Composable
fun rememberLocationAccessCapability(
    isLocationEnabled: IsLocationEnabled,
    requestHistory: PermissionRequestHistory,
    rationalHistory: LocationAccessRationalHistory
): LocationAccessCapability {
    val permissionCapability = rememberLocationPermissionCapability(
        requestHistory = requestHistory,
        rationalHistory = rationalHistory
    )
    val context = LocalContext.current

    return remember(permissionCapability, context, isLocationEnabled) {
        LocationAccessCapabilityImpl(
            locationEnabledProvider = isLocationEnabled,
            openSettings = { context.startActivity(openLocationSettingsIntent) },
            permissionCapability = permissionCapability
        )
    }
}
