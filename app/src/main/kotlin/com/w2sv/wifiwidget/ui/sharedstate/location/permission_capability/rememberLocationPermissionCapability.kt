package com.w2sv.wifiwidget.ui.sharedstate.location.permission_capability

import android.Manifest
import android.os.Build
import androidx.annotation.ChecksSdkIntAtLeast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.permissions.rememberPermissionState
import com.w2sv.androidutils.content.openAppSettings
import com.w2sv.composed.core.CollectFromFlow
import com.w2sv.kotlinutils.makeIf
import com.w2sv.wifiwidget.ui.AppViewModel
import com.w2sv.wifiwidget.ui.util.activityViewModel
import com.w2sv.wifiwidget.ui.util.snackbar.ScopedSnackbarController
import com.w2sv.wifiwidget.ui.util.snackbar.rememberScopedSnackbarController
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map

@Composable
fun rememberLocationPermissionCapability(
    appVM: AppViewModel = activityViewModel(),
    snackbarEmitter: ScopedSnackbarController = rememberScopedSnackbarController()
): LocationPermissionCapability {
    val context = LocalContext.current
    val permissionsState = rememberMultiplePermissionsState(
        listOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    )

    val backgroundPermissionState = makeIf(backgroundLocationAccessGrantRequired) {
        rememberPermissionState(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
    }

    val requestLaunchedBefore by appVM.locationAccessPermissionRequested.collectAsStateWithLifecycle(false)
    val rationalShown by appVM.locationAccessRationalShown.collectAsStateWithLifecycle(true)

    val capability = remember(permissionsState, backgroundPermissionState) {
        LocationPermissionCapabilityImpl(
            foregroundPermissionsState = permissionsState,
            backgroundPermissionState = backgroundPermissionState,
            requestLaunchedBefore = { requestLaunchedBefore },
            saveRequestLaunchedBefore = appVM::saveLocationAccessPermissionRequested,
            rationalAlreadyShown = rationalShown,
            saveRationalShown = appVM::saveLocationAccessRationalShown,
            showSnackbar = { snackbarEmitter.showReplacing { it() } },
            openAppSettings = { context.openAppSettings() }
        )
    }

    // Call capability.onPermissionGranted on new allPermissionsGranted status
    val allPermissionsNewlyGrantedFlow = remember(permissionsState) {
        snapshotFlow { permissionsState.allPermissionsGranted }.drop(1).filter { it }.map { }
    }
    CollectFromFlow(allPermissionsNewlyGrantedFlow) { capability.onPermissionGranted() }

    return capability
}

@get:ChecksSdkIntAtLeast(api = Build.VERSION_CODES.Q)
private val backgroundLocationAccessGrantRequired: Boolean
    get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
