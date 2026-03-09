package com.w2sv.wifiwidget.ui.screen.home.wifistatus

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.w2sv.composed.core.extensions.thenIf
import com.w2sv.composed.core.isPortraitModeActive
import com.w2sv.core.common.R
import com.w2sv.domain.model.wifiproperty.viewdata.WifiPropertyViewData
import com.w2sv.wifiwidget.ui.designsystem.ElevatedIconHeaderCard
import com.w2sv.wifiwidget.ui.designsystem.IconHeader
import com.w2sv.wifiwidget.ui.screen.home.wifistatus.model.WifiState
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

@Composable
fun WifiStatusCard(wifiState: WifiState, modifier: Modifier = Modifier) {
    ElevatedIconHeaderCard(
        iconHeader = IconHeader(
            iconRes = R.drawable.ic_network_check_24,
            stringRes = R.string.wifi_status
        ),
        modifier = modifier,
        content = {
            WifiStatusDisplay(wifiState.status)
            OptionalWifiPropertyList(wifiState.asConnectedOrNull?.propertyViewData.orEmpty().toImmutableList())
        }
    )
}

@Composable
private fun OptionalWifiPropertyList(viewData: ImmutableList<WifiPropertyViewData>, modifier: Modifier = Modifier) {
    AnimatedVisibility(
        visible = viewData.isNotEmpty(),
        modifier = modifier,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically()
    ) {
        WifiPropertyList(
            viewData = viewData,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp)
                .thenIf(
                    condition = isPortraitModeActive,
                    onTrue = { fillMaxHeight(0.32f) }
                )
        )
    }
}
