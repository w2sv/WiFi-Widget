package com.w2sv.widget.ui.state

import androidx.glance.text.TextAlign
import com.w2sv.domain.widget.FontSize
import com.w2sv.domain.widget.WidgetColors
import com.w2sv.domain.widget.WifiPropertyValueAlignment

/** One immutable render pass; row text and clipboard payloads always come from the same snapshot. */
internal data class WidgetPresentationState(
    val wifiState: WidgetWifiState,
    val colors: WidgetColors,
    val backgroundColor: Int,
    val fontSize: FontSize,
    val valueAlignment: WifiPropertyValueAlignment,
    val utilities: WidgetUtilities,
    val renderTime: FormattedRenderTime
) {
    val valueAlign: TextAlign = if (valueAlignment == WifiPropertyValueAlignment.Left) TextAlign.Start else TextAlign.End
}
