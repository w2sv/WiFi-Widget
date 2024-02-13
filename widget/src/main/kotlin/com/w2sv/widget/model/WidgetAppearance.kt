package com.w2sv.widget.model

import android.content.Context
import androidx.annotation.FloatRange

data class WidgetAppearance(
    private val useDynamicColors: Boolean,
    val theme: WidgetTheme,
    @FloatRange(0.0, 1.0) val backgroundOpacity: Float,
    val bottomRow: WidgetBottomRow,
) {
    fun getColors(context: Context): WidgetColors =
        theme.getColors(context, useDynamicColors)
}
