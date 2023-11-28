package com.w2sv.wifiwidget.ui.screens.home.components.wifistatus

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.w2sv.domain.model.WifiStatus
import com.w2sv.wifiwidget.R
import com.w2sv.wifiwidget.ui.components.IconHeader
import com.w2sv.wifiwidget.ui.screens.home.components.HomeScreenCard
import com.w2sv.wifiwidget.ui.screens.home.components.wifistatus.model.WifiState

@Composable
fun WifiStatusCard(
    wifiState: WifiState,
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

            WifiStatusDisplay(wifiStatus = wifiState.status)

            AnimatedVisibility(visible = wifiState.status == WifiStatus.Connected) {
                wifiState.propertyViewData?.let {
                    Column {
                        Spacer(modifier = Modifier.height(12.dp))
                        WifiPropertiesList(
                            propertiesViewData = it,
                            modifier = Modifier.fillMaxHeight(0.22f)
                        )
                    }
                }
            }
        },
        modifier = modifier,
    )
}
