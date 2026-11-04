package com.w2sv.wifiwidget.ui.screen.home

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.w2sv.domain.networking.WifiStatus
import com.w2sv.domain.wifiproperty.presentation.SubscriptableText
import com.w2sv.domain.wifiproperty.presentation.WifiPropertyPresentation
import com.w2sv.wifiwidget.ui.designsystem.theme.ThemeController
import com.w2sv.wifiwidget.ui.screen.home.model.wifistate.WifiState
import com.w2sv.wifiwidget.ui.util.PreviewOf
import com.w2sv.wifiwidget.ui.util.ScreenPreviews
import kotlinx.coroutines.flow.emptyFlow

@Preview
@Composable
private fun DisabledPrev() {
    HomeScreenPreview(WifiState.Disabled)
}

@Preview(name = "Landscape disconnected", device = "spec:width=411dp,height=891dp,orientation=landscape,dpi=420", showSystemUi = true)
@Composable
private fun LandscapeDisconnectedPrev() {
    HomeScreenPreview(WifiState.Disconnected)
}

@Preview(name = "Landscape disabled", device = "spec:width=411dp,height=891dp,orientation=landscape,dpi=420", showSystemUi = true)
@Composable
private fun LandscapeDisabledPrev() {
    HomeScreenPreview(WifiState.Disabled)
}

@Preview(
    name = "Landscape connected without properties",
    device = "spec:width=411dp,height=891dp,orientation=landscape,dpi=420",
    showSystemUi = true
)
@Composable
private fun LandscapeConnectedWithoutPropertiesPrev() {
    HomeScreenPreview(WifiState.Connected(WifiStatus.Connected, emptyList()))
}

@ScreenPreviews
@Composable
private fun ConnectedPrev() {
    HomeScreenPreview(previewConnectedState)
}

private val previewConnectedState =
    WifiState.Connected(
        WifiStatus.Connected,
        listOf(
            WifiPropertyPresentation(SubscriptableText("SSID"), value = "My network"),
            WifiPropertyPresentation(SubscriptableText("BSSID"), value = "02:11:22:33:44:55"),
            WifiPropertyPresentation(SubscriptableText("IP address"), value = "192.168.1.42"),
            WifiPropertyPresentation(
                SubscriptableText("IP", "IPv6"),
                value = "2001:db8:abcd:1234:5678:9abc:def0:1234"
            ),
            WifiPropertyPresentation(
                SubscriptableText("Frequency"),
                value = "5180 MHz",
                subValues = listOf("5 GHz", "Channel 36")
            ),
            WifiPropertyPresentation(SubscriptableText("Link speed"), value = "866 Mbps"),
            WifiPropertyPresentation(SubscriptableText("Gateway"), value = "192.168.1.1"),
            WifiPropertyPresentation(SubscriptableText("DNS"), value = "1.1.1.1")
        )
    )

@Composable
private fun HomeScreenPreview(wifiState: WifiState) {
    PreviewOf {
        HomeScreen(
            themeController = ThemeController.Default,
            wifiState = wifiState,
            pinWidget = {},
            toWidgetConfiguration = {},
            snackbarBuilderFlow = emptyFlow()
        )
    }
}
