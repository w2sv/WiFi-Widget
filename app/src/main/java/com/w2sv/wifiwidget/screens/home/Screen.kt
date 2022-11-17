package com.w2sv.wifiwidget.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun HomeScreen(requestPinWidget: () -> Unit, launchLocationPermissionRequest: () -> Unit) {
    BottomSheetScaffold {
        Column(
            Modifier
                .padding(it)
                .fillMaxHeight()
                .fillMaxWidth(),
            Arrangement.Center,
            Alignment.CenterHorizontally
        ) {
            PinAppWidgetButton(requestPinWidget, launchLocationPermissionRequest)
        }
    }
}

@Composable
@Preview
private fun HomeScreenPreview() {
    HomeScreen({}, {})
}
