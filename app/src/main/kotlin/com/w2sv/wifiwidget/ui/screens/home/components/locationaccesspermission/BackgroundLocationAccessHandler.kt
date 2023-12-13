package com.w2sv.wifiwidget.ui.screens.home.components.locationaccesspermission

import android.Manifest
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import com.w2sv.wifiwidget.R
import com.w2sv.wifiwidget.ui.components.DialogButton
import com.w2sv.wifiwidget.ui.components.InfoIcon
import com.w2sv.wifiwidget.ui.screens.home.components.locationaccesspermission.states.BackgroundLocationAccessPermissionState
import com.w2sv.wifiwidget.ui.utils.styledTextResource
import kotlinx.coroutines.flow.SharedFlow

@Composable
@RequiresApi(Build.VERSION_CODES.Q)
fun BackgroundLocationAccessPermissionHandler(state: BackgroundLocationAccessPermissionState) {
    if (state.showRational.collectAsStateWithLifecycle().value) {
        BackgroundLocationAccessRational(
            launchPermissionRequest = {
                state.launchRequest()
            },
            onDismissRequest = {
                state.showRational(false)
            },
        )
    }
    BackgroundLocationAccessPermissionRequest(trigger = state.launchRequest)
}

@Composable
private fun BackgroundLocationAccessRational(
    launchPermissionRequest: () -> Unit,
    onDismissRequest: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            DialogButton(
                onClick = {
                    launchPermissionRequest()
                    onDismissRequest()
                },
            ) {
                Text(text = stringResource(id = R.string.grant))
            }
        },
        dismissButton = {
            DialogButton(onClick = onDismissRequest) {
                Text(text = stringResource(id = R.string.maybe_later))
            }
        },
        icon = {
            InfoIcon()
        },
        text = {
            Text(text = styledTextResource(id = R.string.background_location_access_rational))
        },
    )
}

@RequiresApi(Build.VERSION_CODES.Q)
@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun BackgroundLocationAccessPermissionRequest(trigger: SharedFlow<Unit>) {
    val permissionState =
        rememberPermissionState(permission = Manifest.permission.ACCESS_BACKGROUND_LOCATION)

    LaunchedEffect(trigger) {
        trigger.collect {
            permissionState.launchPermissionRequest()
        }
    }
}