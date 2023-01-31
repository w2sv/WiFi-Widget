package com.w2sv.wifiwidget.screens.home

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import com.w2sv.wifiwidget.R
import com.w2sv.wifiwidget.ui.JostText

@Composable
fun LocationAccessPermissionDialog(
    onConfirmButtonPressed: () -> Unit,
    onDismissButtonPressed: () -> Unit,
    onDialogAnswered: () -> Unit,
    onDismissRequest: () -> Unit
) {
    AlertDialog(
        icon = {
            Icon(
                imageVector = Icons.Outlined.Info,
                contentDescription = "@null",
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = {
            JostText(
                text = "SSID Retrieval requires Location Access Permission",
                textAlign = TextAlign.Center
            )
        },
        text = {
            JostText(
                text = stringResource(id = R.string.lap_dialog_text)
            )
        },
        confirmButton = {
            ElevatedButton({
                onConfirmButtonPressed()
                onDialogAnswered()
                onDismissRequest()
            }) { JostText(text = "Go ahead") }
        },
        dismissButton = {
            ElevatedButton({
                onDismissButtonPressed()
                onDialogAnswered()
                onDismissRequest()
            }) { JostText(text = "Proceed without SSID") }
        },
        onDismissRequest = onDismissRequest
    )
}

@Composable
fun LocationAccessServiceInformationDialog(onDismissRequest: () -> Unit) {
    AlertDialog(
        icon = {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = "@null",
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = { JostText(text = "Note", textAlign = TextAlign.Center) },
        text = {
            JostText(text = "In order for your SSID to be correctly displayed, you also need to have your device's location service enabled.")
        },
        onDismissRequest = onDismissRequest,
        confirmButton = {
            ElevatedButton(onClick = onDismissRequest) {
                JostText(text = "Got it!")
            }
        }
    )
}