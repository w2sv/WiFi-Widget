package com.w2sv.wifiwidget.ui.screens.home.components.wifistatus

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.w2sv.wifiwidget.R
import com.w2sv.wifiwidget.ui.designsystem.IconHeaderProperties
import com.w2sv.wifiwidget.ui.screens.home.components.HomeScreenCard
import com.w2sv.wifiwidget.ui.screens.home.components.wifistatus.model.WifiState
import com.w2sv.wifiwidget.ui.utils.isLandscapeModeActivated
import com.w2sv.wifiwidget.ui.utils.thenIf

@Composable
fun WifiStatusCard(
    wifiState: WifiState,
    modifier: Modifier = Modifier
) {
    HomeScreenCard(
        iconHeaderProperties = IconHeaderProperties(
            iconRes = R.drawable.ic_network_check_24,
            stringRes = R.string.wifi_status,
        ),
        modifier = modifier,
        content = {
            WifiStatusDisplay(wifiState.status)

            // Display WifiProperties if wifiState is WifiState.Connected
            AnimatedVisibility(visible = wifiState is WifiState.Connected) {
                (wifiState as? WifiState.Connected)?.let {
                    Column {
                        Spacer(modifier = Modifier.height(12.dp))
                        WifiPropertyDisplay(
                            propertiesViewData = it.propertyViewData,
                            modifier = Modifier.thenIf(
                                condition = isLandscapeModeActivated,
                                onFalse = { fillMaxHeight(0.3f) }
                            )
                        )
                    }
                }
            }
        }
    )
}
