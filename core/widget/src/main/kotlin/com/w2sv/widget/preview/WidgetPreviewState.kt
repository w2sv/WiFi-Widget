package com.w2sv.widget.preview

import android.content.Context
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.w2sv.androidutils.graphics.getAlphaSetColor
import com.w2sv.core.resources.R as ResourcesR
import com.w2sv.domain.networking.WifiStatus
import com.w2sv.domain.widget.WidgetConfig
import com.w2sv.domain.wifiproperty.WifiProperty
import com.w2sv.domain.wifiproperty.presentation.SubscriptableText
import com.w2sv.domain.wifiproperty.presentation.WifiPropertyPresentation
import com.w2sv.widget.ui.state.FormattedRenderTime
import com.w2sv.widget.ui.state.WidgetPresentationState
import com.w2sv.widget.ui.state.WidgetPropertyRow
import com.w2sv.widget.ui.state.WidgetUtilities
import com.w2sv.widget.ui.state.WidgetWifiState
import com.w2sv.widget.ui.theme.resolve
import java.util.Calendar
import java.util.TimeZone

internal val pinPreviewSize = DpSize(250.dp, 140.dp)
internal val launcherPreviewSizes = setOf(pinPreviewSize)
private val previewDate = Calendar.getInstance()
    .apply { set(2024, Calendar.JANUARY, 7, 10, 30) }
    .time

/** Sample values use the requested selection and order without observing a real network. */
internal fun widgetPreviewState(
    context: Context,
    config: WidgetConfig = WidgetConfig.default,
    status: WifiStatus = WifiStatus.Connected
): WidgetPresentationState {
    val appearance = config.appearance
    val colors = appearance.coloring.resolve(context)
    val wifiState = if (status.isConnected) {
        WidgetWifiState.Connected(
            status = status,
            rows = config
                .enabledProperties
                .filter { property -> property !is WifiProperty.IpProperty || config.enabledIpSettings(property).isNotEmpty() }
                .map { property -> WidgetPropertyRow(property, property.samplePresentation(context)) }
        )
    } else {
        WidgetWifiState.Disconnected(status)
    }
    return WidgetPresentationState(
        wifiState = wifiState,
        colors = colors,
        backgroundColor = getAlphaSetColor(colors.background, appearance.backgroundOpacity),
        fontSize = appearance.fontSize,
        valueAlignment = appearance.propertyValueAlignment,
        utilities = WidgetUtilities(config),
        renderTime = FormattedRenderTime(context, previewDate, TimeZone.getDefault())
    )
}

private fun WifiProperty.samplePresentation(context: Context): WifiPropertyPresentation {
    val label = if (this is WifiProperty.IpProperty) {
        SubscriptableText(context.getString(ResourcesR.string.ip), context.getString(subscriptResId))
    } else {
        SubscriptableText(context.getString(labelRes))
    }
    val value = when (this) {
        WifiProperty.SSID -> "Coffee Shop Wi-Fi"
        WifiProperty.BSSID -> "02:00:00:12:34:56"
        WifiProperty.Gateway, WifiProperty.DHCP -> "192.168.1.1"
        WifiProperty.DNS -> "2001:db8:1234:5678:abcd:ef01:2345:6789"
        WifiProperty.Frequency -> "5,180 MHz"
        WifiProperty.Channel -> "36"
        WifiProperty.LinkSpeed -> "866 Mbps"
        WifiProperty.RSSI -> "-54 dBm"
        WifiProperty.SignalStrength -> "Excellent"
        WifiProperty.Standard, WifiProperty.Generation -> "Wi-Fi 6"
        WifiProperty.Security -> "WPA3"
        WifiProperty.ISP -> "Example ISP"
        WifiProperty.ASN -> "AS64500"
        WifiProperty.Location -> "Berlin, Germany"
        WifiProperty.IpCoordinates -> "52.5200, 13.4050"
        WifiProperty.NAT64Prefix -> "64:ff9b::/96"
        WifiProperty.LoopbackIp -> "127.0.0.1"
        WifiProperty.SiteLocalIp -> "192.168.1.42"
        WifiProperty.LinkLocalIp -> "fe80::a1b2:c3d4"
        WifiProperty.ULA -> "fd12:3456:789a::42"
        WifiProperty.MulticastIp -> "224.0.0.1"
        WifiProperty.GUA -> "2001:db8::42"
        WifiProperty.PublicIp -> "203.0.113.24"
    }
    return WifiPropertyPresentation(
        label = label,
        value = value,
        subValues = if (this == WifiProperty.SiteLocalIp) listOf("/24", "255.255.255.0") else emptyList()
    )
}
