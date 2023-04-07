package com.w2sv.wifiwidget.ui.screens.home

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
import com.w2sv.androidutils.extensions.reset
import com.w2sv.common.WifiProperty
import com.w2sv.widget.WifiWidgetProvider
import com.w2sv.wifiwidget.R
import com.w2sv.wifiwidget.ui.shared.DialogButton
import com.w2sv.wifiwidget.ui.shared.JostText
import com.w2sv.wifiwidget.ui.shared.WifiWidgetTheme

@Preview
@Composable
private fun Prev() {
    WifiWidgetTheme {
        LocationAccessPermissionDialog(Modifier, "Proceed without SSID", {}, {}, {}, {})
    }
}

enum class LocationAccessPermissionDialogTrigger {
    PinWidgetButtonPress,
    SSIDCheck
}

@Composable
fun LocationAccessPermissionDialog(
    viewModel: HomeActivity.ViewModel = viewModel(),
    trigger: () -> LocationAccessPermissionDialogTrigger?
) {
    val context = LocalContext.current
    val lapRequestLauncher = context.requireCastActivity<HomeActivity>().lapRequestLauncher

    trigger()?.let {
        when (it) {
            LocationAccessPermissionDialogTrigger.PinWidgetButtonPress -> {
                LocationAccessPermissionDialog(
                    dismissButtonText = "Proceed without SSID",
                    onConfirmButtonPressed = {
                        lapRequestLauncher.requestPermissionAndSetSSIDFlagCorrespondingly(
                            viewModel,
                            onGranted = {
                                viewModel.widgetPropertyStateMap.apply()
                            },
                            onRequestDismissed = {
                                WifiWidgetProvider.pinWidget(context)
                            }
                        )
                    },
                    onDismissButtonPressed = {
                        WifiWidgetProvider.pinWidget(context)
                    },
                    onAnyButtonPressed = {
                        viewModel.lapDialogAnswered = true
                    },
                    onDismiss = {
                        viewModel.lapDialogTrigger.reset()
                    }
                )
            }

            LocationAccessPermissionDialogTrigger.SSIDCheck -> LocationAccessPermissionDialog(
                dismissButtonText = "Never mind",
                onConfirmButtonPressed = {
                    lapRequestLauncher.requestPermissionAndSetSSIDFlagCorrespondingly(viewModel)
                },
                onDismissButtonPressed = {
                    viewModel.widgetPropertyStateMap[WifiProperty.SSID.name] = false
                },
                onAnyButtonPressed = { viewModel.lapDialogAnswered = true },
                onDismiss = {
                    viewModel.lapDialogTrigger.reset()
                }
            )
        }
    }
}

@Composable
private fun LocationAccessPermissionDialog(
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