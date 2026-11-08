@file:OptIn(androidx.glance.preview.ExperimentalGlancePreviewApi::class)

package com.w2sv.widget.ui.preview

import androidx.compose.runtime.Composable
import androidx.glance.LocalContext
import androidx.glance.preview.Preview
import com.w2sv.domain.networking.WifiStatus
import com.w2sv.domain.preferences.Theme
import com.w2sv.domain.widget.FontSize
import com.w2sv.domain.widget.WidgetConfig
import com.w2sv.domain.widget.WifiPropertyValueAlignment
import com.w2sv.domain.wifiproperty.WifiProperty
import com.w2sv.widget.preview.widgetPreviewState
import com.w2sv.widget.ui.content.WifiWidgetContent
import com.w2sv.widget.ui.state.WidgetWifiState

private val previewConfig = WidgetConfig.default.run {
    copy(
        appearance = appearance.copy(
            coloring = appearance.coloring.copy(
                preset = appearance.coloring.preset.copy(useDynamicColors = false, theme = Theme.Dark)
            )
        )
    )
}

@Preview(widthDp = 160, heightDp = 100)
@Composable
internal fun ConnectedSmallPreview() =
    WifiWidgetContent(widgetPreviewState(LocalContext.current, config = previewConfig))

@Preview(widthDp = 250, heightDp = 140)
@Composable
internal fun ConnectedMediumPreview() =
    WifiWidgetContent(
        widgetPreviewState(LocalContext.current, config = previewConfig, status = WifiStatus.ConnectedInactive)
    )

@Preview(widthDp = 250, heightDp = 140)
@Composable
internal fun ConnectedLeftAlignedPreview() =
    WifiWidgetContent(
        widgetPreviewState(
            LocalContext.current,
            config = previewConfig.copy(
                appearance = previewConfig.appearance.copy(propertyValueAlignment = WifiPropertyValueAlignment.Left)
            )
        )
    )

@Preview(widthDp = 320, heightDp = 220)
@Composable
internal fun ConnectedLargePreview() =
    WifiWidgetContent(
        widgetPreviewState(LocalContext.current, config = previewConfig, status = WifiStatus.ConnectedNoInternet)
    )

@Preview(widthDp = 250, heightDp = 140)
@Composable
internal fun DisabledPreview() =
    WifiWidgetContent(widgetPreviewState(LocalContext.current, config = previewConfig, status = WifiStatus.Disabled))

@Preview(widthDp = 250, heightDp = 140)
@Composable
internal fun NotConnectedPreview() =
    WifiWidgetContent(
        widgetPreviewState(LocalContext.current, config = previewConfig, status = WifiStatus.NotConnected)
    )

@Preview(widthDp = 160, heightDp = 100)
@Composable
internal fun DenseSmallPreview() =
    WifiWidgetContent(
        widgetPreviewState(
            LocalContext.current,
            config = previewConfig.withUpdatedPropertyEnablement(WifiProperty.SSID, true)
        )
    )

@Preview(widthDp = 100, heightDp = 40)
@Composable
internal fun TinyConnectedPreview() {
    val state = widgetPreviewState(LocalContext.current, config = previewConfig)
    val connected = state.wifiState as WidgetWifiState.Connected
    WifiWidgetContent(state.copy(wifiState = connected.copy(rows = connected.rows.take(1)), fontSize = FontSize.Small))
}

@Preview(widthDp = 100, heightDp = 40)
@Composable
internal fun TinyDisabledPreview() =
    WifiWidgetContent(widgetPreviewState(LocalContext.current, config = previewConfig, status = WifiStatus.Disabled))

@Preview(widthDp = 250, heightDp = 40)
@Composable
internal fun ShortDisabledPreview() =
    WifiWidgetContent(widgetPreviewState(LocalContext.current, config = previewConfig, status = WifiStatus.Disabled))

@Preview(widthDp = 160, heightDp = 100)
@Composable
internal fun CompactDisabledPreview() =
    WifiWidgetContent(widgetPreviewState(LocalContext.current, config = previewConfig, status = WifiStatus.Disabled))
