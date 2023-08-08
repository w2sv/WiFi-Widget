package com.w2sv.wifiwidget.ui.screens.home.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.w2sv.data.model.WifiStatus

@Composable
fun WifiConnectionInfoCard(
    wifiStatus: WifiStatus,
    wifiPropertiesViewData: List<WifiPropertyViewData>?,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier
) {
    ElevatedCard(modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            WifiStatusDisplay(wifiStatus = wifiStatus)
            Spacer(modifier = Modifier.height(24.dp))
            AnimatedVisibility(visible = wifiPropertiesViewData != null) {
                wifiPropertiesViewData?.let {
                    WifiPropertiesList(
                        propertiesViewData = it,
                        snackbarHostState = snackbarHostState,
                    )
                }
            }
        }
    }
}