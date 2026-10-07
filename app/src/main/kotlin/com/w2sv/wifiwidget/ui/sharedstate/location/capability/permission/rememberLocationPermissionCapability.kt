package com.w2sv.wifiwidget.ui.sharedstate.location.capability.permission

import android.Manifest
import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.ChecksSdkIntAtLeast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.w2sv.androidutils.content.openAppSettings
import com.w2sv.augmentedpermissions.PermissionRequestHistory
import com.w2sv.augmentedpermissions.PermissionState
import com.w2sv.augmentedpermissions.rememberPermissionState
import com.w2sv.composed.material3.SnackbarLauncher
import com.w2sv.composed.material3.rememberSnackbarLauncher
import com.w2sv.composed.material3.replaceCurrentWith
import com.w2sv.composed.runtime.CollectFromFlow
import com.w2sv.kotlinutils.makeIf
import com.w2sv.wifiwidget.ui.LocalSnackbarHostState
import com.w2sv.wifiwidget.ui.sharedstate.location.LocationAccessRationalHistory
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filter
import slimber.log.d

@Composable
fun rememberLocationPermissionCapability(
    requestHistory: PermissionRequestHistory,
    rationalHistory: LocationAccessRationalHistory,
    snackbarLauncher: SnackbarLauncher = rememberSnackbarLauncher(LocalSnackbarHostState.current)
): LocationPermissionCapability {
    val foregroundPermissionState = rememberForegroundLocationPermissionState(requestHistory)
    val backgroundPermissionState = rememberBackgroundLocationPermissionState()
    val capability = rememberLocationPermissionCapability(
        rationalHistory = rationalHistory,
        snackbarLauncher = snackbarLauncher,
        foregroundPermissionState = foregroundPermissionState,
        backgroundPermissionState = backgroundPermissionState
    )

    ObserveForegroundPermissionGrants(foregroundPermissionState, capability)
    LogLocationPermissionCapability(capability)

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
    makeIf(backgroundLocationAccessGrantRequired) {
        rememberPermissionState(
            permission = Manifest.permission.ACCESS_BACKGROUND_LOCATION,
            requestHistory = null
        )
    }

@Composable
private fun rememberLocationPermissionCapability(
    rationalHistory: LocationAccessRationalHistory,
    snackbarLauncher: SnackbarLauncher,
    foregroundPermissionState: PermissionState,
    backgroundPermissionState: PermissionState?
): LocationPermissionCapabilityImpl {
    val context = LocalContext.current
    val rationalWasShown by rationalHistory.wasShownBefore.collectAsStateWithLifecycle(true)
    val currentRationalShown = rememberUpdatedState(rationalWasShown)
    val currentSaveRationalShown = rememberUpdatedState(rationalHistory.recordWasShown)
    val currentSnackbarLauncher = rememberUpdatedState(snackbarLauncher)
    val currentContext = rememberUpdatedState(context)

    return remember(foregroundPermissionState, backgroundPermissionState) {
        LocationPermissionCapabilityImpl(
            foregroundPermissionsState = foregroundPermissionState,
            backgroundPermissionState = backgroundPermissionState,
            rationalShown = { currentRationalShown.value },
            saveRationalShown = { currentSaveRationalShown.value() },
            showSnackbar = { currentSnackbarLauncher.value.replaceCurrentWith { it() } },
            openAppSettings = { currentContext.value.openAppSettings() }
        )
    }
}

@Composable
private fun ObserveForegroundPermissionGrants(foregroundPermissionState: PermissionState, capability: LocationPermissionCapability) {
    val newlyGranted = remember(foregroundPermissionState) {
        snapshotFlow { foregroundPermissionState.isGranted }.drop(1).filter { it }
    }
    CollectFromFlow(newlyGranted) { capability.onPermissionGranted() }
}

@Composable
private fun LogLocationPermissionCapability(capability: LocationPermissionCapability) {
    val snapshot = LocationPermissionCapabilitySnapshot(
        foregroundGranted = capability.foregroundPermissionsGranted,
        backgroundMissing = capability.isBackgroundPermissionMissing,
        foregroundRationaleVisible = capability.showForegroundRational,
        backgroundRationaleVisible = capability.showBackgroundRational
    )
    LaunchedEffect(snapshot) { d { "Location permissions: $snapshot" } }
}

/** For logging only */
private data class LocationPermissionCapabilitySnapshot(
    val foregroundGranted: Boolean,
    val backgroundMissing: Boolean,
    val foregroundRationaleVisible: Boolean,
    val backgroundRationaleVisible: Boolean
)

@get:ChecksSdkIntAtLeast(api = Build.VERSION_CODES.Q)
private val backgroundLocationAccessGrantRequired: Boolean
    get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
