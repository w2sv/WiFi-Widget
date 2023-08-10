package com.w2sv.wifiwidget.ui.screens.home.components.wifi_connection_info

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.SnackbarVisuals
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.w2sv.data.model.WifiStatus
import com.w2sv.wifiwidget.R
import com.w2sv.wifiwidget.ui.screens.home.CardHeader

@Composable
fun WifiConnectionInfoCard(
    wifiStatus: WifiStatus,
    wifiPropertiesViewData: List<WifiPropertyViewData>?,
    showSnackbar: (SnackbarVisuals) -> Unit,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier,
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CardHeader(iconRes = R.drawable.ic_network_check_24, header = "WiFi Status")
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