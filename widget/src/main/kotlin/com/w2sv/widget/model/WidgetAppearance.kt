package com.w2sv.widget.model

import android.content.Context
import androidx.annotation.FloatRange
import androidx.core.graphics.ColorUtils
import com.w2sv.common.extensions.toRGBChannelInt

data class WidgetAppearance(
    val theme: WidgetTheme,
    @FloatRange(0.0, 1.0) val opacity: Float,
    val displayLastRefreshDateTime: Boolean
) {
    fun getBackgroundOpacityIntegratedColors(context: Context): WidgetColors {
        val rawColors = theme.getColors(context)

        return WidgetColors(
            background = ColorUtils.setAlphaComponent(
                rawColors.background,
                opacity.toRGBChannelInt()
            ),
            labels = rawColors.labels,
            other = rawColors.other
        )
    }
}