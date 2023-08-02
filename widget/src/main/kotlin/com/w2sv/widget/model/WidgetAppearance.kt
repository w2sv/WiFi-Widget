package com.w2sv.widget.model

import androidx.annotation.FloatRange

data class WidgetAppearance(
    val theme: WidgetTheme,
    @FloatRange(0.0, 1.0) val backgroundOpacity: Float,
    val displayLastRefreshDateTime: Boolean,
    val buttons: WidgetButtons
)