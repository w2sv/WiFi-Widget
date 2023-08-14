package com.w2sv.wifiwidget.ui.screens.home.components.locationaccesspermission

import android.Manifest
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.w2sv.androidutils.coroutines.reset
import com.w2sv.common.utils.isLaunchingSuppressed
import kotlinx.coroutines.launch

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun LocationAccessPermissionRequest(
    lapUIState: LocationAccessPermissionUIState,
    onGranted: suspend (Context) -> Unit,
    onDenied: suspend (Context) -> Unit
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val permissionState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
        ),
        onPermissionsResult = { permissionToGranted ->
            scope.launch {
                when (permissionToGranted.values.all { it }) {
                    true -> {
                        onGranted(context)
                        lapUIState.onGranted()
                    }

                    false -> onDenied(context)
                }
                lapUIState.requestLaunchingAction.reset()
            }
        }
    )

    LaunchedEffect(Unit) {
        when (permissionState.allPermissionsGranted) {
            true -> {
                onGranted(context)
                lapUIState.requestLaunchingAction.reset()
            }

            false -> {
                when (permissionState.isLaunchingSuppressed(launchedBefore = lapUIState.requestLaunched)) {
                    true -> {
                        lapUIState.onRequestLaunchingSuppressed(context)
                        lapUIState.requestLaunchingAction.reset()
                    }

                    false -> {
                        permissionState.launchMultiplePermissionRequest()
                        lapUIState.onRequestLaunched()
                    }
                }
            }
        }
    }
}