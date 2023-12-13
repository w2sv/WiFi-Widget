package com.w2sv.wifiwidget.ui.screens.home.components.locationaccesspermission

import android.Manifest
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.w2sv.androidutils.generic.goToAppSettings
import com.w2sv.wifiwidget.ui.components.AppSnackbarVisuals
import com.w2sv.wifiwidget.ui.components.LocalSnackbarHostState
import com.w2sv.wifiwidget.ui.components.SnackbarAction
import com.w2sv.wifiwidget.ui.components.showSnackbarAndDismissCurrentIfApplicable
import com.w2sv.wifiwidget.ui.utils.isLaunchingSuppressed

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun LocationAccessPermissionRequest(lapState: LocationAccessPermissionState) {
    val permissionState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
        ),
        onPermissionsResult = {
            lapState.onRequestLaunched()
        }
    )

    // Set lapState.isGranted on permission status change
    LaunchedEffect(permissionState.allPermissionsGranted) {
        lapState.setIsGranted(permissionState.allPermissionsGranted)
    }

    val context = LocalContext.current
    val snackbarHostState = LocalSnackbarHostState.current
    LaunchedEffect(lapState.requestTrigger) {
        lapState.requestTrigger.collect { trigger ->
            trigger?.let {
                if (permissionState.isLaunchingSuppressed(launchedBefore = lapState.requestLaunchedBefore.value)) {
                    snackbarHostState.showSnackbarAndDismissCurrentIfApplicable(
                        AppSnackbarVisuals(
                            msg = context.getString(com.w2sv.wifiwidget.R.string.you_need_to_go_to_the_app_settings_and_grant_location_access_permission),
                            kind = com.w2sv.wifiwidget.ui.components.SnackbarKind.Error,
                            action = SnackbarAction(
                                label = context.getString(com.w2sv.wifiwidget.R.string.go_to_settings),
                                callback = {
                                    goToAppSettings(context)
                                },
                            ),
                        ),
                    )
                    lapState.setRequestTrigger(null)
                } else {
                    permissionState.launchMultiplePermissionRequest()
                }
            }
        }
    }
}
