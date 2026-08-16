package com.w2sv.widget.ui

import androidx.compose.ui.graphics.Color
import com.w2sv.androidutils.graphics.getAlphaSetColor
import com.w2sv.domain.model.networking.WifiStatus
import com.w2sv.domain.model.widget.WidgetColors
import com.w2sv.domain.model.widget.WidgetConfig
import com.w2sv.domain.model.wifiproperty.viewdata.SubscriptableText
import com.w2sv.domain.model.wifiproperty.viewdata.WifiPropertyViewData
import java.time.Instant

internal data class WifiWidgetState(
    val config: WidgetConfig,
    val colors: WidgetColors,
    val status: WifiStatus,
    val properties: List<WifiPropertyViewData>,
    val renderedAt: Instant
) {
    val backgroundColor: Color
        get() = Color(
            getAlphaSetColor(
                colors.background,
                config.appearance.backgroundOpacity
            )
        )
}

internal fun previewWifiWidgetState(): WifiWidgetState =
    WifiWidgetState(
        config = WidgetConfig.default,
        colors = WidgetColors(
            background = 0xFFF7F9FF.toInt(),
            primary = 0xFF00639A.toInt(),
            secondary = 0xFF30343B.toInt()
        ),
        status = WifiStatus.Connected,
        properties = listOf(
            WifiPropertyViewData(SubscriptableText("SSID"), "Coffee Shop WiFi"),
            WifiPropertyViewData(SubscriptableText("IP", "LAN"), "192.168.1.42", listOf("/24")),
            WifiPropertyViewData(SubscriptableText("Public IP"), "203.0.113.24"),
            WifiPropertyViewData(SubscriptableText("Gateway"), "192.168.1.1")
        ),
        renderedAt = Instant.parse("2026-08-16T10:30:00Z")
    )
