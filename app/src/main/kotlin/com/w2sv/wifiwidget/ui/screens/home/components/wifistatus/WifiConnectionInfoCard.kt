package com.w2sv.wifiwidget.ui.screens.home.components.wifistatus

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.w2sv.domain.model.WidgetWifiProperty
import com.w2sv.domain.model.WifiStatus
import com.w2sv.wifiwidget.R
import com.w2sv.wifiwidget.ui.components.IconHeader
import com.w2sv.wifiwidget.ui.screens.home.components.HomeScreenCard

@Composable
fun WifiConnectionInfoCard(
    wifiStatus: WifiStatus,
    wifiPropertiesViewData: List<WidgetWifiProperty.ValueViewData>?,
    modifier: Modifier = Modifier,
) {
    HomeScreenCard(
        content = {
            IconHeader(
                iconRes = R.drawable.ic_network_check_24,
                headerRes = R.string.wifi_status,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
            Spacer(modifier = Modifier.height(16.dp))

            WifiStatusDisplay(wifiStatus = wifiStatus)
            Spacer(modifier = Modifier.height(12.dp))

            AnimatedVisibility(visible = wifiPropertiesViewData != null) {
                wifiPropertiesViewData?.let {
                    WifiPropertiesList(
                        propertiesViewData = it,
                    )
                }
            }
        },
        modifier = modifier,
    )
}
