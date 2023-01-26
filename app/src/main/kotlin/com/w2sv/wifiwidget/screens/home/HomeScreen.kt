package com.w2sv.wifiwidget.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.w2sv.wifiwidget.ui.AppTopBar
import com.w2sv.wifiwidget.ui.JostText
import kotlinx.coroutines.flow.update

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
internal fun HomeScreen() {
    Scaffold(topBar = { AppTopBar() }) { paddingValues ->
        Column(
            Modifier
                .padding(paddingValues)
                .fillMaxHeight()
                .fillMaxWidth(),
            Arrangement.SpaceEvenly,
            Alignment.CenterHorizontally
        ) {
            PinWidgetButton()
            PropertiesConfigurationDialogInflationButton()
            EventualLocationAccessServiceInformationDialog()
        }
    }
}

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