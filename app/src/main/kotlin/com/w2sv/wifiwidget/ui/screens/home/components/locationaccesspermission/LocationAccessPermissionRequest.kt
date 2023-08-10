package com.w2sv.wifiwidget.ui.screens.home.components.locationaccesspermission

import android.Manifest
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.w2sv.androidutils.coroutines.reset
import com.w2sv.androidutils.generic.goToAppSettings
import com.w2sv.common.utils.isLaunchingSuppressed
import com.w2sv.wifiwidget.R
import com.w2sv.wifiwidget.ui.components.AppSnackbarVisuals
import com.w2sv.wifiwidget.ui.components.SnackbarAction
import com.w2sv.wifiwidget.ui.components.SnackbarKind
import com.w2sv.wifiwidget.ui.components.showSnackbarAndDismissCurrentIfApplicable
import com.w2sv.wifiwidget.ui.viewmodels.HomeScreenViewModel
import kotlinx.coroutines.launch
import slimber.log.i

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun LocationAccessPermissionRequest(
    onGranted: suspend (Context) -> Unit,
    onDenied: suspend (Context) -> Unit,
    homeScreenVM: HomeScreenViewModel = viewModel()
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
                        homeScreenVM.triggerWifiPropertiesViewDataRefresh()
                        if (backgroundLocationAccessGrantRequired) {
                            homeScreenVM.showBackgroundLocationAccessRational.value = true
                        }
                    }

                    false -> onDenied(context)
                }
                homeScreenVM.lapRequestTrigger.reset()
            }
        }
    )

    LaunchedEffect(key1 = permissionState.permissions) {
        i { "All permissions granted = ${permissionState.allPermissionsGranted}" }
        when (permissionState.allPermissionsGranted) {
            true -> {
                onGranted(context)
                homeScreenVM.lapRequestTrigger.reset()
            }

            false -> {
                when (permissionState.isLaunchingSuppressed(homeScreenVM.lapRequestLaunchedAtLeastOnce)) {
                    true -> {
                        homeScreenVM.snackbarHostState.showSnackbarAndDismissCurrentIfApplicable(
                            AppSnackbarVisuals(
                                context.getString(R.string.you_need_to_go_to_the_app_settings_and_grant_location_access_permission),
                                kind = SnackbarKind.Error,
                                action = SnackbarAction(
                                    label = context.getString(R.string.go_to_settings),
                                    callback = {
                                        goToAppSettings(context)
                                    }
                                )
                            )
                        )
                        homeScreenVM.lapRequestTrigger.reset()
                    }

                    false -> {
                        permissionState.launchMultiplePermissionRequest()
                        homeScreenVM.onLocationAccessPermissionRequested()
                    }
                }
            }
        }
    }
}