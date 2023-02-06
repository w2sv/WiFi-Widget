package com.w2sv.wifiwidget.ui.home

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.w2sv.androidutils.extensions.requireCastActivity
import com.w2sv.widget.WifiWidgetProvider
import com.w2sv.wifiwidget.R
import com.w2sv.wifiwidget.activities.HomeActivity
import com.w2sv.wifiwidget.ui.DialogButton
import com.w2sv.wifiwidget.ui.JostText
import com.w2sv.wifiwidget.ui.WifiWidgetTheme

enum class LocationAccessPermissionDialogTrigger {
    PinWidgetButtonPress,
    SSIDCheck
}

@Composable
fun LocationAccessPermissionDialog(
    trigger: LocationAccessPermissionDialogTrigger,
    viewModel: HomeActivity.ViewModel = viewModel(),
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val activity = context.requireCastActivity<HomeActivity>()

    when (trigger) {
        LocationAccessPermissionDialogTrigger.PinWidgetButtonPress -> {
            StatelessLocationAccessPermissionDialog(
                dismissButtonText = "Proceed without SSID",
                onConfirmButtonPressed = {
                    activity.lapRequestLauncher.requestPermissionIfRequired(
                        onGranted = {
                            viewModel.setSSIDState(true, updateRequiringUpdate = false)
                            viewModel.updateWidgetConfiguration()
                        },
                        onRequestDismissed = {
                            viewModel.setSSIDState(false, updateRequiringUpdate = false)
                            WifiWidgetProvider.pinWidget(context)
                        }
                    )
                },
                onDismissButtonPressed = {
                    WifiWidgetProvider.pinWidget(context)
                },
                onAnyButtonPressed = {
                    viewModel.onLapDialogAnswered()
                },
                onDismiss = onDismiss
            )
        }

        LocationAccessPermissionDialogTrigger.SSIDCheck -> StatelessLocationAccessPermissionDialog(
            dismissButtonText = "Never mind",
            onConfirmButtonPressed = {
                activity.lapRequestLauncher.requestPermissionIfRequired(
                    onDenied = { viewModel.setSSIDState(false) },
                    onGranted = { viewModel.setSSIDState(true) }
                )
            },
            onDismissButtonPressed = { viewModel.setSSIDState(false) },
            onAnyButtonPressed = { viewModel.onLapDialogAnswered() },
            onDismiss = onDismiss
        )
    }
}

@Composable
private fun StatelessLocationAccessPermissionDialog(
    modifier: Modifier = Modifier,
    dismissButtonText: String,
    onConfirmButtonPressed: () -> Unit,
    onDismissButtonPressed: () -> Unit,
    onAnyButtonPressed: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        modifier = modifier,
        icon = {
            Icon(
                imageVector = Icons.Outlined.Info,
                contentDescription = "@null",
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
                {
                    onConfirmButtonPressed()
                    onAnyButtonPressed()
                    onDismiss()
                },
                modifier = Modifier.fillMaxWidth()
            ) { JostText(text = stringResource(R.string.go_ahead)) }
        },
        dismissButton = {
            DialogButton({
                onDismissButtonPressed()
                onAnyButtonPressed()
                onDismiss()
            }, modifier = Modifier.fillMaxWidth()) { JostText(text = dismissButtonText) }
        },
        onDismissRequest = onDismiss
    )
}

@Preview
@Composable
private fun LocationAccessPermissionDialogPrev() {
    WifiWidgetTheme {
        StatelessLocationAccessPermissionDialog(Modifier, "Proceed without SSID", {}, {}, {}, {})
    }
}