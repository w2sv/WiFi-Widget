package com.w2sv.wifiwidget.ui.screens.home.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarVisuals
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.w2sv.data.model.WifiStatus
import com.w2sv.wifiwidget.R
import com.w2sv.wifiwidget.ui.components.JostText

@Composable
fun WifiConnectionInfoCard(
    wifiStatus: WifiStatus,
    wifiPropertiesViewData: List<WifiPropertyViewData>?,
    showSnackbar: (SnackbarVisuals) -> Unit,
    modifier: Modifier = Modifier
) {
    ElevatedCard(modifier, elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            JostText(
                text = stringResource(R.string.wifi_status),
                color = MaterialTheme.colorScheme.tertiary,
                fontSize = 20.sp
            )
            Spacer(modifier = Modifier.height(16.dp))

            WifiStatusDisplay(wifiStatus = wifiStatus)
            Spacer(modifier = Modifier.height(12.dp))

            AnimatedVisibility(visible = wifiPropertiesViewData != null) {
                wifiPropertiesViewData?.let {
                    WifiPropertiesList(
                        propertiesViewData = it,
                        showSnackbar = showSnackbar,
                    )
                }
            }
        }
    }
}