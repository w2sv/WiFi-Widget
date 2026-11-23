package com.w2sv.wifiwidget.ui.location.capability.access

import android.Manifest
import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import com.w2sv.androidutils.content.openAppSettings
import com.w2sv.augmentedpermissions.PermissionRequestHistory
import com.w2sv.augmentedpermissions.PermissionState
import com.w2sv.augmentedpermissions.rememberPermissionState
import com.w2sv.common.android.SettingsIntents
import com.w2sv.common.android.requiresBackgroundLocationPermission
import com.w2sv.domain.location.IsLocationEnabled
import com.w2sv.kotlinutils.makeIf
import slimber.log.d

@Composable
internal fun rememberLocationAccessCapability(
    isLocationEnabled: IsLocationEnabled,
    requestHistory: PermissionRequestHistory
): LocationAccessCapability {
    val foregroundPermissionState = rememberForegroundLocationPermissionState(requestHistory)
    val backgroundPermissionState = rememberBackgroundLocationPermissionState()
    val capability = rememberLocationAccessCapability(
        foregroundPermissionState = foregroundPermissionState,
        backgroundPermissionState = backgroundPermissionState,
        isLocationEnabled = isLocationEnabled
    )

    val permissionLevel = capability.permissionLevel
    LaunchedEffect(permissionLevel) { d { "Location permissions: $permissionLevel" } }

    return capability
}

@Composable
private fun rememberForegroundLocationPermissionState(requestHistory: PermissionRequestHistory): PermissionState =
    rememberPermissionState(
        permissions = listOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
        ),
        requestHistory = requestHistory
    )

@SuppressLint("InlinedApi")
@Composable
private fun rememberBackgroundLocationPermissionState(): PermissionState? =
    makeIf(requiresBackgroundLocationPermission) {
        rememberPermissionState(
            permission = Manifest.permission.ACCESS_BACKGROUND_LOCATION,
            requestHistory = null
        )
    }

@Composable
private fun rememberLocationAccessCapability(
    foregroundPermissionState: PermissionState,
    backgroundPermissionState: PermissionState?,
    isLocationEnabled: IsLocationEnabled
): LocationAccessCapabilityImpl {
    val context = LocalContext.current
    val currentContext = rememberUpdatedState(context)

    return remember(foregroundPermissionState, backgroundPermissionState, isLocationEnabled) {
        LocationAccessCapabilityImpl(
            foregroundPermissionsState = foregroundPermissionState,
            backgroundPermissionState = backgroundPermissionState,
            launchAppSettings = { currentContext.value.openAppSettings() },
            locationEnabledProvider = isLocationEnabled,
            openLocationServiceSettings = { currentContext.value.startActivity(SettingsIntents.location) }
        )
    }
}
