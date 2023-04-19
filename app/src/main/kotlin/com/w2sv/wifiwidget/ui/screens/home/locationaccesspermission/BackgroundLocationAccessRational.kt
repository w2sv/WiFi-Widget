package com.w2sv.wifiwidget.ui.screens.home.locationaccesspermission

import android.Manifest
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.rememberPermissionState
import com.w2sv.wifiwidget.R
import com.w2sv.wifiwidget.ui.screens.home.HomeScreenViewModel
import com.w2sv.wifiwidget.ui.shared.DialogButton
import com.w2sv.wifiwidget.ui.shared.InfoIcon
import com.w2sv.wifiwidget.ui.shared.JostText
import com.w2sv.wifiwidget.ui.shared.WifiWidgetTheme

val BACKGROUND_LOCATION_ACCESS_GRANT_REQUIRED: Boolean =
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q

@RequiresApi(Build.VERSION_CODES.Q)
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun BackgroundLocationAccessRational(homeScreenViewModel: HomeScreenViewModel = viewModel()) {
    val backgroundAccessPermissionState: PermissionState =
        rememberPermissionState(permission = Manifest.permission.ACCESS_BACKGROUND_LOCATION)

    BackgroundLocationAccessRational {
        homeScreenViewModel.showBackgroundLocationAccessRational.value = false
        backgroundAccessPermissionState.launchPermissionRequest()
    }
}

@Composable
private fun BackgroundLocationAccessRational(onProceed: () -> Unit) {
    AlertDialog(
        onDismissRequest = onProceed,
        confirmButton = {
            DialogButton(onClick = onProceed) {
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

@Preview
@Composable
private fun Prev() {
    WifiWidgetTheme {
        BackgroundLocationAccessRational {}
    }
}