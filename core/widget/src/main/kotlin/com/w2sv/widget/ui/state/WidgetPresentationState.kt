package com.w2sv.widget.ui.state

import androidx.glance.text.TextAlign
import com.w2sv.domain.widget.FontSize
import com.w2sv.domain.widget.WifiPropertyValueAlignment
import com.w2sv.widget.ui.theme.WidgetRenderColors

/** One immutable render pass; row text and clipboard payloads always come from the same snapshot. */
internal data class WidgetPresentationState(
    val wifiState: WidgetWifiState,
    val colors: WidgetRenderColors,
    val fontSize: FontSize,
    val valueAlignment: WifiPropertyValueAlignment,
    val utilities: WidgetUtilities,
    val renderTime: FormattedRenderTime
) {
    val valueAlign: TextAlign = if (valueAlignment == WifiPropertyValueAlignment.Left) TextAlign.Start else TextAlign.End
}
