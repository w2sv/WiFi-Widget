package com.w2sv.wifiwidget.screens.home

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
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
                text = "SSID Retrieval requires Location Access",
                textAlign = TextAlign.Center
            )
        },
        text = {
            JostText(
                text = buildAnnotatedString {
                    append("If you want your SSID to be displayed amongst the WiFi properties, you'll have to grant the app")
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(" location access")
                    }
                    append(".")
                }
            )
        },
        confirmButton = {
            ElevatedButton({
                onConfirmButtonPressed()
                onDialogAnswered()
                onDismissRequest()
            }) { JostText(text = "Let's do it") }
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
            JostText(text = "In order for your SSID to be correctly displayed, you need to have your device's location service enabled.")
        },
        onDismissRequest = onDismissRequest,
        confirmButton = {
            ElevatedButton(onClick = onDismissRequest) {
                JostText(text = "Got it!")
            }
        })
}