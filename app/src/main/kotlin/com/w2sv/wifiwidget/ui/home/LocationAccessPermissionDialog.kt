package com.w2sv.wifiwidget.ui.home

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
import com.w2sv.wifiwidget.R
import com.w2sv.wifiwidget.ui.DialogButton
import com.w2sv.wifiwidget.ui.JostText
import com.w2sv.wifiwidget.ui.WifiWidgetTheme

@Composable
fun LocationAccessPermissionDialog(
    dismissButtonText: String,
    onConfirmButtonPressed: () -> Unit,
    onDismissButtonPressed: () -> Unit,
    onAnyButtonPressed: () -> Unit,
    onCloseDialog: () -> Unit
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
                text = stringResource(id = R.string.lap_dialog_text), textAlign = TextAlign.Center
            )
        },
        confirmButton = {
            DialogButton({
                onConfirmButtonPressed()
                onAnyButtonPressed()
                onCloseDialog()
            }, modifier = Modifier.fillMaxWidth()) { JostText(text = "Go ahead") }
        },
        dismissButton = {
            DialogButton({
                onDismissButtonPressed()
                onAnyButtonPressed()
                onCloseDialog()
            }, modifier = Modifier.fillMaxWidth()) { JostText(text = dismissButtonText) }
        },
        onDismissRequest = onCloseDialog
    )
}

@Preview
@Composable
private fun LocationAccessPermissionDialogPrev() {
    WifiWidgetTheme {
        LocationAccessPermissionDialog("Proceed without SSID", {}, {}, {}, {})
    }
}