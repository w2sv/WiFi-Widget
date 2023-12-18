package com.w2sv.wifiwidget.ui.screens.home.components.locationaccesspermission

import android.Manifest
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.w2sv.androidutils.generic.goToAppSettings
import com.w2sv.domain.model.WidgetWifiProperty
import com.w2sv.wifiwidget.R
import com.w2sv.wifiwidget.ui.components.AppSnackbarVisuals
import com.w2sv.wifiwidget.ui.components.DialogButton
import com.w2sv.wifiwidget.ui.components.InfoIcon
import com.w2sv.wifiwidget.ui.components.LocalSnackbarHostState
import com.w2sv.wifiwidget.ui.components.SnackbarAction
import com.w2sv.wifiwidget.ui.components.SnackbarKind
import com.w2sv.wifiwidget.ui.components.showSnackbarAndDismissCurrentIfApplicable
import com.w2sv.wifiwidget.ui.screens.home.components.locationaccesspermission.states.LocationAccessPermissionState
import com.w2sv.wifiwidget.ui.theme.AppTheme
import com.w2sv.wifiwidget.ui.utils.isLaunchingSuppressed
import com.w2sv.wifiwidget.ui.utils.styledTextResource

sealed interface LocationAccessPermissionRequestTrigger {
    data object InitialAppLaunch : LocationAccessPermissionRequestTrigger
    class PropertyCheckChange(val property: WidgetWifiProperty.NonIP.LocationAccessRequiring) :
        LocationAccessPermissionRequestTrigger
}

sealed interface LocationAccessPermissionStatus {
    data object NotGranted: LocationAccessPermissionStatus
    class Granted(val trigger: LocationAccessPermissionRequestTrigger?): LocationAccessPermissionStatus
}

@Composable
fun LocationAccessPermissionHandler(state: LocationAccessPermissionState) {
    if (state.showRational.collectAsStateWithLifecycle().value) {
        LocationAccessPermissionRational(
            onProceed = {
                state.onRationalShown()
            },
        )
    }
    LocationAccessPermissionRequest(state = state)
}

@Composable
private fun LocationAccessPermissionRational(
    onProceed: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AlertDialog(
        modifier = modifier,
        icon = {
            InfoIcon()
        },
        text = {
            Text(
                text = styledTextResource(id = R.string.location_access_permission_rational),
                textAlign = TextAlign.Center,
            )
        },
        confirmButton = {
            DialogButton(
                onClick = onProceed,
                modifier = Modifier.fillMaxWidth(),
            ) { Text(text = stringResource(R.string.understood)) }
        },
        onDismissRequest = onProceed,
    )
}

@Preview
@Composable
private fun Prev() {
    AppTheme {
        LocationAccessPermissionRational({})
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun LocationAccessPermissionRequest(state: LocationAccessPermissionState) {
    val permissionState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
        ),
        onPermissionsResult = { result ->
            state.onRequestResult(granted = result.values.all { it })
        }
    )

    // Set lapState.isGranted on permission status change
    LaunchedEffect(permissionState.allPermissionsGranted) {
        state.setStatus(permissionState.allPermissionsGranted)
    }

    val context = LocalContext.current
    val snackbarHostState = LocalSnackbarHostState.current
    LaunchedEffect(state.requestTrigger) {
        state.requestTrigger.collect {
            if (permissionState.isLaunchingSuppressed(launchedBefore = state.requestLaunchedBefore.value)) {
                snackbarHostState.showSnackbarAndDismissCurrentIfApplicable(
                    AppSnackbarVisuals(
                        msg = context.getString(R.string.you_need_to_go_to_the_app_settings_and_grant_location_access_permission),
                        kind = SnackbarKind.Error,
                        action = SnackbarAction(
                            label = context.getString(R.string.go_to_settings),
                            callback = {
                                goToAppSettings(context)
                            },
                        ),
                    ),
                )
            } else {
                permissionState.launchMultiplePermissionRequest()
            }
        }
    }
}
