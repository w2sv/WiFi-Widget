package com.w2sv.wifiwidget.ui.screens.home

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.w2sv.androidutils.extensions.reset
import com.w2sv.common.preferences.PreferencesKey
import com.w2sv.wifiwidget.R
import com.w2sv.wifiwidget.ui.shared.DialogButton
import com.w2sv.wifiwidget.ui.shared.JostText
import com.w2sv.wifiwidget.ui.shared.WifiWidgetTheme

@Preview
@Composable
private fun Prev() {
    WifiWidgetTheme {
        LocationAccessPermissionRational(LocationAccessPermissionRequestTrigger.SSIDCheck)
    }
}

enum class LocationAccessPermissionRequestTrigger {
    PinWidgetButtonPress,
    SSIDCheck
}

@Composable
fun LocationAccessPermissionRational(
    trigger: LocationAccessPermissionRequestTrigger,
    modifier: Modifier = Modifier,
    viewModel: HomeScreenViewModel = viewModel()
) {
    AlertDialog(
        modifier = modifier,
        icon = {
            Icon(
                imageVector = Icons.Outlined.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = {
            JostText(
                text = stringResource(R.string.lap_dialog_title),
                textAlign = TextAlign.Center
            )
        },
        text = {
            JostText(
                text = stringResource(id = R.string.lap_dialog_text),
                textAlign = TextAlign.Center
            )
        },
        confirmButton = {
            DialogButton(
                onClick = {
                    viewModel.saveToDataStore(
                        PreferencesKey.LOCATION_ACCESS_PERMISSION_DIALOG_ANSWERED,
                        true
                    )
                    viewModel.lapDialogTrigger.reset()
                    viewModel.lapRequestTrigger.value = trigger
                },
                modifier = Modifier.fillMaxWidth()
            ) { JostText(text = stringResource(R.string.proceed)) }
        },
        onDismissRequest = {
            viewModel.lapDialogTrigger.reset()
        }
    )
}