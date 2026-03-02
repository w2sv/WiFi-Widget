package com.w2sv.wifiwidget.ui.screen.home.wifistatus

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.w2sv.composed.core.extensions.thenIf
import com.w2sv.composed.core.isPortraitModeActive
import com.w2sv.wifiwidget.R
import com.w2sv.wifiwidget.ui.designsystem.ElevatedIconHeaderCard
import com.w2sv.wifiwidget.ui.designsystem.IconHeader
import com.w2sv.wifiwidget.ui.screen.home.wifistatus.model.WifiState
import kotlinx.collections.immutable.toImmutableList

@Composable
fun WifiStatusCard(wifiState: WifiState, modifier: Modifier = Modifier) {
    ElevatedIconHeaderCard(
        iconHeader = remember {
            IconHeader(
                iconRes = R.drawable.ic_network_check_24,
                stringRes = R.string.wifi_status
            )
        },
        modifier = modifier,
        content = {
            WifiStatusDisplay(wifiState.status)

            // Display WifiProperties if wifiState is WifiState.Connected
            AnimatedVisibility(visible = wifiState is WifiState.Connected) {
                wifiState.connectedOrNull?.let {
                    WifiPropertyDisplay(
                        wifiPropertyViewData = it.wifiPropertyViewData.toImmutableList(),
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
