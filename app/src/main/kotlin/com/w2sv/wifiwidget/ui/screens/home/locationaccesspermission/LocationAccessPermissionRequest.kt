package com.w2sv.wifiwidget.ui.screens.home.locationaccesspermission

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
import com.w2sv.androidutils.extensions.reset
import com.w2sv.androidutils.extensions.showToast
import com.w2sv.common.WifiProperty
import com.w2sv.common.extensions.launchingSuppressed
import com.w2sv.common.preferences.PreferencesKey
import com.w2sv.widget.WidgetProvider
import com.w2sv.wifiwidget.R
import com.w2sv.wifiwidget.ui.screens.home.HomeScreenViewModel
import com.w2sv.wifiwidget.ui.screens.home.widgetconfiguration.WidgetConfigurationViewModel
import kotlinx.coroutines.launch
import slimber.log.i

@Composable
fun LocationAccessPermissionRequest(
    trigger: LocationAccessPermissionRequestTrigger,
    widgetConfigurationViewModel: WidgetConfigurationViewModel = viewModel()
) {
    when (trigger) {
        LocationAccessPermissionRequestTrigger.PinWidgetButtonPress -> LocationAccessPermissionRequest(
            onGranted = {
                widgetConfigurationViewModel.nonAppliedWifiPropertyFlags[WifiProperty.SSID] = true
                widgetConfigurationViewModel.nonAppliedWifiPropertyFlags.sync()
                WidgetProvider.pinWidget(it)
            },
            onDenied = {
                WidgetProvider.pinWidget(it)
            }
        )

        LocationAccessPermissionRequestTrigger.SSIDCheck -> LocationAccessPermissionRequest(
            onGranted = {
                widgetConfigurationViewModel.nonAppliedWifiPropertyFlags[WifiProperty.SSID] = true
            },
            onDenied = {}
        )
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun LocationAccessPermissionRequest(
    onGranted: suspend (Context) -> Unit,
    onDenied: suspend (Context) -> Unit,
    viewModel: HomeScreenViewModel = viewModel()
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
                        if (BACKGROUND_LOCATION_ACCESS_GRANT_REQUIRED) {
                            viewModel.showBackgroundLocationAccessRational.value = true
                        }
                    }

                    false -> onDenied(context)
                }
                viewModel.lapRequestTrigger.reset()
            }
        }
    )

    LaunchedEffect(key1 = permissionState.permissions) {
        i { "All permissions granted = ${permissionState.allPermissionsGranted}" }
        when (permissionState.allPermissionsGranted) {
            true -> {
                onGranted(context)
                viewModel.lapRequestTrigger.reset()
            }

            false -> {
                when (permissionState.launchingSuppressed(viewModel.lapRequestLaunchedAtLeastOnce)) {
                    true -> {
                        context.showToast(
                            context.getString(R.string.go_to_app_settings_and_grant_location_access_permission),
                            Toast.LENGTH_LONG
                        )
                        viewModel.lapRequestTrigger.reset()
                    }

                    false -> {
                        permissionState.launchMultiplePermissionRequest()
                        viewModel.saveToDataStore(
                            PreferencesKey.LOCATION_ACCESS_PERMISSION_REQUESTED_AT_LEAST_ONCE,
                            true
                        )
                    }
                }
            }
        }
    }
}