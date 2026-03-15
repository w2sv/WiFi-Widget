package com.w2sv.wifiwidget.ui.screen.home.components.wifistatus

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.w2sv.common.utils.openWifiSettingsIntent
import com.w2sv.composed.core.extensions.thenIf
import com.w2sv.composed.core.isPortraitModeActive
import com.w2sv.core.common.R
import com.w2sv.domain.model.networking.WifiStatus
import com.w2sv.domain.model.wifiproperty.viewdata.SubscriptableText
import com.w2sv.domain.model.wifiproperty.viewdata.WifiPropertyViewData
import com.w2sv.wifiwidget.ui.designsystem.ElevatedIconHeaderCard
import com.w2sv.wifiwidget.ui.designsystem.IconHeader
import com.w2sv.wifiwidget.ui.screen.home.model.wifistate.WifiState
import com.w2sv.wifiwidget.ui.util.PreviewOf
import com.w2sv.wifiwidget.ui.util.VerticallyAnimatedVisibility
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

@Preview
@Composable
private fun Prev() {
    PreviewOf {
        WifiStatusCard(
            wifiState = WifiState.Connected(
                WifiStatus.Connected,
                listOf(
                    WifiPropertyViewData(SubscriptableText("Property1"), value = "Value1"),
                    WifiPropertyViewData(SubscriptableText("Property2"), value = "Value2")
                )
            )
        )
    }
}

@Composable
fun WifiStatusDisplay(wifiStatus: WifiStatus, modifier: Modifier = Modifier) {
    val context: Context = LocalContext.current

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clickable(
                onClickLabel = context.getString(R.string.go_to_wifi_settings_cd),
                onClick = { context.startActivity(openWifiSettingsIntent) }
            )
    ) {
        Icon(
            painter = painterResource(id = wifiStatus.iconRes),
            contentDescription = null,
            modifier = Modifier
                .size(42.dp)
                .padding(bottom = 2.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Text(text = stringResource(id = wifiStatus.labelRes))
    }
}

@Composable
private fun OptionalWifiPropertyList(viewData: ImmutableList<WifiPropertyViewData>, modifier: Modifier = Modifier) {
    VerticallyAnimatedVisibility(
        visible = viewData.isNotEmpty(),
        modifier = modifier
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
