package com.w2sv.wifiwidget.ui.sharedstate.location.permission_capability

import android.Manifest
import android.os.Build
import androidx.annotation.ChecksSdkIntAtLeast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.w2sv.androidutils.content.openAppSettings
import com.w2sv.augmentedpermissions.rememberPermissionState
import com.w2sv.composed.material3.SnackbarLauncher
import com.w2sv.composed.material3.rememberSnackbarLauncher
import com.w2sv.composed.material3.replaceCurrentWith
import com.w2sv.composed.runtime.CollectFromFlow
import com.w2sv.kotlinutils.makeIf
import com.w2sv.wifiwidget.ui.AppViewModel
import com.w2sv.wifiwidget.ui.LocalSnackbarHostState
import com.w2sv.wifiwidget.ui.util.activityViewModel
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filter
import slimber.log.d

@Composable
fun rememberLocationPermissionCapability(
    appVM: AppViewModel = activityViewModel(),
    snackbarLauncher: SnackbarLauncher = rememberSnackbarLauncher(LocalSnackbarHostState.current)
): LocationPermissionCapability {
    val context = LocalContext.current
    val permissionsState = rememberPermissionState(
        permissions = listOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
        ),
        requestHistory = appVM.locationAccessPermissionHistory
    )

    val backgroundPermissionState = makeIf(backgroundLocationAccessGrantRequired) {
        rememberPermissionState(
            permission = Manifest.permission.ACCESS_BACKGROUND_LOCATION,
            requestHistory = null
        )
    }

    val rationalShown by appVM.locationAccessRationalShown.collectAsStateWithLifecycle(true)

    val capability = remember(permissionsState, backgroundPermissionState) {
        LocationPermissionCapabilityImpl(
            foregroundPermissionsState = permissionsState,
            backgroundPermissionState = backgroundPermissionState,
            rationalShown = { rationalShown },
            saveRationalShown = appVM::saveLocationAccessRationalShown,
            showSnackbar = { snackbarLauncher.replaceCurrentWith { it() } },
            openAppSettings = { context.openAppSettings() }
        )
    }

    // Call capability.onPermissionGranted on new granted status
    val allPermissionsNewlyGrantedFlow = remember(permissionsState) {
        snapshotFlow { permissionsState.isGranted }.drop(1).filter { it }
    }
    CollectFromFlow(allPermissionsNewlyGrantedFlow) { capability.onPermissionGranted() }

    LaunchedEffect(
        capability.showBackgroundRational,
        capability.showForegroundRational,
        capability.foregroundPermissionsGranted,
        capability.isBackgroundPermissionMissing
    ) {
        d {
            """
        capability.showBackgroundRational = ${capability.showBackgroundRational}
        capability.showForegroundRational = ${capability.showForegroundRational}
        capability.foregroundPermissionsGranted = ${capability.foregroundPermissionsGranted}
        capability.isBackgroundPermissionMissing = ${capability.isBackgroundPermissionMissing}
            """.trimIndent()
        }
    }

    return capability
}

@get:ChecksSdkIntAtLeast(api = Build.VERSION_CODES.Q)
private val backgroundLocationAccessGrantRequired: Boolean
    get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
