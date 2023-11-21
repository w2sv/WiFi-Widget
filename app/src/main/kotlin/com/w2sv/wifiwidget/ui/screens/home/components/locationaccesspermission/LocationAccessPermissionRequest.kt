package com.w2sv.wifiwidget.ui.screens.home.components.locationaccesspermission

import android.Manifest
import android.content.Context
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.w2sv.androidutils.generic.goToAppSettings
import com.w2sv.wifiwidget.R
import com.w2sv.wifiwidget.ui.components.AppSnackbarVisuals
import com.w2sv.wifiwidget.ui.components.LocalSnackbarHostState
import com.w2sv.wifiwidget.ui.components.SnackbarAction
import com.w2sv.wifiwidget.ui.components.SnackbarKind
import com.w2sv.wifiwidget.ui.components.showSnackbarAndDismissCurrentIfApplicable
import com.w2sv.wifiwidget.ui.utils.isLaunchingSuppressed
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun LocationAccessPermissionRequest(
    lapUIState: LocationAccessPermissionState,
    onGranted: suspend (Context) -> Unit,
    onDenied: suspend (Context) -> Unit,
    snackbarHostState: SnackbarHostState = LocalSnackbarHostState.current,
    context: Context = LocalContext.current,
    scope: CoroutineScope = rememberCoroutineScope()
) {
    val permissionState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
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
                lapUIState.setRequestLaunchingAction(null)
            }
        },
    )

    LaunchedEffect(Unit) {
        when (permissionState.allPermissionsGranted) {
            true -> {
                onGranted(context)
                lapUIState.setRequestLaunchingAction(null)
            }

            false -> {
                when (permissionState.isLaunchingSuppressed(launchedBefore = lapUIState.requestLaunched)) {
                    true -> {
                        scope.launch {
                            snackbarHostState.showSnackbarAndDismissCurrentIfApplicable(
                                AppSnackbarVisuals(
                                    context.getString(R.string.you_need_to_go_to_the_app_settings_and_grant_location_access_permission),
                                    kind = SnackbarKind.Error,
                                    action = SnackbarAction(
                                        label = context.getString(R.string.go_to_settings),
                                        callback = {
                                            goToAppSettings(context)
                                        },
                                    ),
                                ),
                            )
                        }
                        lapUIState.setRequestLaunchingAction(null)
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
