package com.w2sv.wifiwidget.ui.sharedstate.location.permission_capability

import android.Manifest
import android.os.Build
import androidx.annotation.ChecksSdkIntAtLeast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.permissions.rememberPermissionState
import com.w2sv.androidutils.content.openAppSettings
import com.w2sv.kotlinutils.makeIf
import com.w2sv.widget.WifiWidgetProvider
import com.w2sv.wifiwidget.ui.AppViewModel
import com.w2sv.wifiwidget.ui.sharedstate.location.OnLocationAccessGrant
import com.w2sv.wifiwidget.ui.util.ScopedSnackbarEmitter
import com.w2sv.wifiwidget.ui.util.activityViewModel
import com.w2sv.wifiwidget.ui.util.rememberScopedSnackbarEmitter

@Composable
fun rememberLocationPermissionCapability(
    appVM: AppViewModel = activityViewModel(),
    snackbarEmitter: ScopedSnackbarEmitter = rememberScopedSnackbarEmitter()
): LocationPermissionCapabilityImpl {
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

    val capability = remember(
        permissionsState,
        backgroundPermissionState
    ) {
        LocationPermissionCapabilityImpl(
            foregroundPermissionsState = permissionsState,
            backgroundPermissionState = backgroundPermissionState,
            requestLaunchedBefore = { requestLaunchedBefore },
            saveRequestLaunchedBefore = appVM::saveLocationAccessPermissionRequested,
            rationalAlreadyShown = rationalShown,
            saveRationalShown = appVM::saveLocationAccessRationalShown,
            showSnackbar = { snackbarEmitter.dismissCurrentAndShow { it() } },
            openAppSettings = { context.openAppSettings() }
        )
    }

    // 🔥 SINGLE OBSERVATION POINT
    LaunchedEffect(permissionsState.allPermissionsGranted) {
        if (!permissionsState.allPermissionsGranted) return@LaunchedEffect

        capability.consumeOnGrantAction()?.let { action ->
            when (action) {
                OnLocationAccessGrant.TriggerWidgetDataRefresh ->
                    WifiWidgetProvider.triggerDataRefresh(context)

                OnLocationAccessGrant.EnableLocationAccessDependentProperties ->
                    Unit // hook into your reversible config here

                is OnLocationAccessGrant.EnableProperty ->
                    Unit // enable specific property
            }
        }

        capability.maybeShowBackgroundRational()
    }

    return capability
}

@get:ChecksSdkIntAtLeast(api = Build.VERSION_CODES.Q)
private val backgroundLocationAccessGrantRequired: Boolean
    get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
