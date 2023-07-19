package com.w2sv.wifiwidget.ui.screens.home.components.locationaccesspermission

import android.Manifest
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.rememberPermissionState
import com.w2sv.wifiwidget.R
import com.w2sv.wifiwidget.ui.components.DialogButton
import com.w2sv.wifiwidget.ui.components.InfoIcon
import com.w2sv.wifiwidget.ui.components.JostText
import com.w2sv.wifiwidget.ui.theme.AppTheme

@RequiresApi(Build.VERSION_CODES.Q)
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun BackgroundLocationAccessRational(onDismissRequest: () -> Unit) {
    val backgroundAccessPermissionState: PermissionState =
        rememberPermissionState(permission = Manifest.permission.ACCESS_BACKGROUND_LOCATION)

    val proceed: () -> Unit = {
        backgroundAccessPermissionState.launchPermissionRequest()
        onDismissRequest()
    }

    AlertDialog(
        onDismissRequest = proceed,
        confirmButton = {
            DialogButton(onClick = proceed) {
                Text(text = stringResource(id = R.string.got_it))
            }
        },
        icon = {
            InfoIcon()
        },
        text = {
            JostText(text = stringResource(id = R.string.background_location_access_rational))
        }
    )
}

@RequiresApi(Build.VERSION_CODES.Q)
@Preview
@Composable
private fun Prev() {
    AppTheme {
        BackgroundLocationAccessRational {}
    }
}