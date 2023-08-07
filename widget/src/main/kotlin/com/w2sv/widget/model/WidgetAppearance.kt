package com.w2sv.widget.model

import android.content.Context
import androidx.annotation.FloatRange

data class WidgetAppearance(
    val useDynamicColors: Boolean,
    val theme: WidgetTheme,
    @FloatRange(0.0, 1.0) val backgroundOpacity: Float,
    val displayLastRefreshDateTime: Boolean,
    val buttons: WidgetButtons
) {
    fun getColors(context: Context): WidgetColors =
        theme.getColors(context, useDynamicColors)
}