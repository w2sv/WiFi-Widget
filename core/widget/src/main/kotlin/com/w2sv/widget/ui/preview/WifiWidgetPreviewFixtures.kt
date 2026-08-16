package com.w2sv.widget.ui.preview

import com.w2sv.domain.model.networking.WifiStatus
import com.w2sv.domain.model.widget.WidgetColors
import com.w2sv.domain.model.widget.WidgetConfig
import com.w2sv.domain.model.wifiproperty.viewdata.SubscriptableText
import com.w2sv.domain.model.wifiproperty.viewdata.WifiPropertyViewData
import com.w2sv.widget.ui.model.WifiWidgetState
import java.time.Instant

internal fun connectedWifiWidgetPreviewState(): WifiWidgetState =
    previewWifiWidgetState(
        status = WifiStatus.Connected,
        properties = listOf(
            WifiPropertyViewData(SubscriptableText("SSID"), "Coffee Shop WiFi"),
            WifiPropertyViewData(SubscriptableText("IP", "LAN"), "192.168.1.42", listOf("/24")),
            WifiPropertyViewData(SubscriptableText("Public IP"), "203.0.113.24"),
            WifiPropertyViewData(SubscriptableText("Gateway"), "192.168.1.1")
        )
    )

internal fun disabledWifiWidgetPreviewState(): WifiWidgetState =
    previewWifiWidgetState(status = WifiStatus.Disabled)

internal fun notConnectedWifiWidgetPreviewState(): WifiWidgetState =
    previewWifiWidgetState(status = WifiStatus.NotConnected)

private fun previewWifiWidgetState(status: WifiStatus, properties: List<WifiPropertyViewData> = emptyList()): WifiWidgetState =
    WifiWidgetState(
        config = WidgetConfig.default,
        colors = WidgetColors(
            background = 0xFFF7F9FF.toInt(),
            primary = 0xFF00639A.toInt(),
            secondary = 0xFF30343B.toInt()
        ),
        status = status,
        properties = properties,
        renderedAt = Instant.parse("2026-08-16T10:30:00Z")
    )
