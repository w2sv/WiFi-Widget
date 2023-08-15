package com.w2sv.wifiwidget.ui.screens.home.components.wifi_status

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SnackbarVisuals
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.w2sv.data.model.WifiStatus
import com.w2sv.wifiwidget.R
import com.w2sv.wifiwidget.ui.components.IconHeader
import com.w2sv.wifiwidget.ui.screens.home.components.HomeScreenCard

@Composable
fun WifiConnectionInfoCard(
    wifiStatus: WifiStatus,
    wifiPropertiesViewData: List<WifiPropertyViewData>?,
    showSnackbar: (SnackbarVisuals) -> Unit,
    modifier: Modifier = Modifier
) {
    HomeScreenCard(
        content = {
            IconHeader(
                iconRes = R.drawable.ic_network_check_24,
                headerRes = R.string.wifi_status,
                modifier = Modifier.padding(horizontal = 16.dp)
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
        },
        modifier = modifier
    )
}