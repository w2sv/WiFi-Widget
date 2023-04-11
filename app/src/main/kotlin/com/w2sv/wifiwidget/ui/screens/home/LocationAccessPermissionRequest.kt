package com.w2sv.wifiwidget.ui.screens.home

import android.Manifest
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
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
import com.w2sv.wifiwidget.ui.screens.home.widgetconfiguration.WidgetConfigurationViewModel
import kotlinx.coroutines.launch

@Composable
fun LocationAccessPermissionRequest(
    trigger: LocationAccessPermissionDialogTrigger,
    widgetConfigurationViewModel: WidgetConfigurationViewModel = viewModel()
) {
    val context = LocalContext.current

    when (trigger) {
        LocationAccessPermissionDialogTrigger.PinWidgetButtonPress -> LocationAccessPermissionRequest(
            onGranted = {
                widgetConfigurationViewModel.widgetPropertyStateMap[WifiProperty.SSID] = true
                widgetConfigurationViewModel.widgetPropertyStateMap.apply()
                WidgetProvider.pinWidget(context)
            },
            onDenied = {
                WidgetProvider.pinWidget(context)
            }
        )

        LocationAccessPermissionDialogTrigger.SSIDCheck -> LocationAccessPermissionRequest(
            onGranted = {
                widgetConfigurationViewModel.widgetPropertyStateMap[WifiProperty.SSID] = true
            },
            onDenied = {}
        )
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun LocationAccessPermissionRequest(
    onGranted: suspend () -> Unit,
    onDenied: suspend () -> Unit,
    homeScreenViewModel: HomeScreenViewModel = viewModel()
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
                    true -> onGranted()
                    false -> onDenied()
                }
                homeScreenViewModel.lapRequestTrigger.reset()
                homeScreenViewModel.dataStoreRepository.save(
                    true,
                    PreferencesKey.LOCATION_ACCESS_PERMISSION_REQUEST_LAUNCHED_AT_LEAST_ONCE
                )
            }
        }
    )

    when (permissionState.allPermissionsGranted) {
        true -> LaunchedEffect(
            key1 = permissionState.permissions,
            block = { onGranted() }
        )

        false -> {
            if (permissionState.launchingSuppressed(homeScreenViewModel.lapRequestLaunchedAtLeastOnce)) {
                context.showToast(
                    stringResource(R.string.go_to_app_settings_and_grant_location_access_permission),
                    Toast.LENGTH_LONG
                )
                homeScreenViewModel.lapRequestTrigger.reset()
            } else {
                LaunchedEffect(
                    key1 = permissionState.permissions,
                    block = { permissionState.launchMultiplePermissionRequest() }
                )
            }
        }
    }
}