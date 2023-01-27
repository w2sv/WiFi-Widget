package com.w2sv.wifiwidget.screens.home

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.viewmodel.compose.viewModel
import com.w2sv.wifiwidget.ui.JostText
import kotlinx.coroutines.flow.update

@Composable
fun EventualLocationAccessServiceInformationDialog() {
    val viewModel = viewModel<HomeActivity.ViewModel>()

    if (viewModel.lapJustGranted.collectAsState().value) {
        LocationAccessServiceInformationDialog {
            viewModel.lapJustGranted.update { false }
        }
    }
}

@Composable
private fun LocationAccessServiceInformationDialog(onDismissRequest: () -> Unit) {
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