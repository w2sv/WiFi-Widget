package com.w2sv.wifiwidget.ui.location.capability.access

import androidx.compose.runtime.staticCompositionLocalOf
import com.w2sv.wifiwidget.ui.location.LocationPermissionLevel
import kotlinx.coroutines.flow.Flow

internal val LocalLocationAccessCapability = staticCompositionLocalOf<LocationAccessCapability> {
    error("LocationAccessCapability not provided")
}

/**
 * Android-facing access to live location permissions, system location services, permission-request
 * results and Settings navigation. Unlike an immutable availability snapshot, this capability
 * provides operations; it owns neither permission-flow transitions nor screen presentation.
 */
internal interface LocationAccessCapability {
    val locationServicesEnabled: Boolean
    val locationServicesEnabledFlow: Flow<Boolean>
    fun openLocationSettings()

    // ========= Permission State =========
    val permissionLevel: LocationPermissionLevel
    val foregroundRequestSuppressed: Boolean
    val foregroundRequestResults: Flow<Boolean>
    val backgroundRequestResults: Flow<Boolean>
    fun requestPermission()
    fun launchBackgroundPermission()
    fun openAppSettings()
}
