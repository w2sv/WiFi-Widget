package com.w2sv.wifiwidget.ui.screens.home.components.locationaccesspermission

import android.Manifest
import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.w2sv.androidutils.coroutines.reset
import com.w2sv.androidutils.notifying.showToast
import com.w2sv.common.data.repositories.PreferencesRepository
import com.w2sv.common.extensions.launchingSuppressed
import com.w2sv.wifiwidget.R
import com.w2sv.wifiwidget.ui.screens.home.HomeScreenViewModel
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
                when (permissionState.launchingSuppressed(homeScreenVM.lapRequestLaunchedAtLeastOnce)) {
                    true -> {
                        context.showToast(
                            context.getString(R.string.go_to_app_settings_and_grant_location_access_permission),
                            Toast.LENGTH_LONG
                        )
                        homeScreenVM.lapRequestTrigger.reset()
                    }

                    false -> {
                        permissionState.launchMultiplePermissionRequest()
                        homeScreenVM.saveToDataStore(
                            PreferencesRepository.Key.LOCATION_ACCESS_PERMISSION_REQUESTED_AT_LEAST_ONCE,
                            true
                        )
                    }
                }
            }
        }
    }
}