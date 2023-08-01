package com.w2sv.widget.model

import androidx.annotation.FloatRange

data class WidgetAppearance(
    val theme: WidgetTheme,
    @FloatRange(0.0, 1.0) val opacity: Float,
    val displayLastRefreshDateTime: Boolean
)