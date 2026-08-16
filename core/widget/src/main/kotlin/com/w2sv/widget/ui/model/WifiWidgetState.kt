package com.w2sv.widget.ui.model

import androidx.compose.ui.graphics.Color
import com.w2sv.androidutils.graphics.getAlphaSetColor
import com.w2sv.domain.model.networking.WifiStatus
import com.w2sv.domain.model.widget.WidgetColors
import com.w2sv.domain.model.widget.WidgetConfig
import com.w2sv.domain.model.widget.WidgetUtility
import com.w2sv.domain.model.wifiproperty.viewdata.WifiPropertyViewData
import java.time.Instant

internal data class WifiWidgetState(
    private val config: WidgetConfig,
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

    val fontSize by config.appearance::fontSize
    val propertyValueAlignment by config.appearance::propertyValueAlignment

    val enabledUtilities: List<WidgetUtility>
        get() = config.enabledUtilities()
}
