package com.w2sv.wifiwidget.ui.screens.home.components.locationaccesspermission

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.w2sv.wifiwidget.R
import com.w2sv.wifiwidget.ui.components.DialogButton
import com.w2sv.wifiwidget.ui.components.InfoIcon
import com.w2sv.wifiwidget.ui.utils.styledTextResource

@Composable
fun BackgroundLocationAccessRational(
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
