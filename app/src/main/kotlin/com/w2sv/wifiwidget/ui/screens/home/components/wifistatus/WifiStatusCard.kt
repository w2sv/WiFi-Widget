package com.w2sv.wifiwidget.ui.screens.home.components.wifistatus

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.w2sv.composed.extensions.thenIf
import com.w2sv.composed.isPortraitModeActive
import com.w2sv.wifiwidget.R
import com.w2sv.wifiwidget.ui.designsystem.ElevatedIconHeaderCard
import com.w2sv.wifiwidget.ui.designsystem.IconHeaderProperties
import com.w2sv.wifiwidget.ui.screens.home.components.wifistatus.model.WifiState

@Composable
fun WifiStatusCard(wifiState: WifiState, modifier: Modifier = Modifier) {
    ElevatedIconHeaderCard(
        iconHeaderProperties = IconHeaderProperties(
            iconRes = R.drawable.ic_network_check_24,
            stringRes = R.string.wifi_status
        ),
        modifier = modifier,
        content = {
            WifiStatusDisplay(wifiState.status)

            // Display WifiProperties if wifiState is WifiState.Connected
            AnimatedVisibility(visible = wifiState is WifiState.Connected) {
                wifiState.connectedOrNull?.let {
                    WifiPropertyDisplay(
                        propertiesViewData = it.viewDataFlow,
                        modifier = Modifier
                            .padding(top = 12.dp)
                            .thenIf(
                                condition = isPortraitModeActive,
                                onTrue = { fillMaxHeight(0.32f) }
                            )
                    )
                }
            }
        }
    )
}
