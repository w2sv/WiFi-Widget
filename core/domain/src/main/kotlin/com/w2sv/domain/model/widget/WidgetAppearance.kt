package com.w2sv.domain.model.widget

import androidx.annotation.FloatRange

data class WidgetAppearance(
    val coloring: WidgetColoring = WidgetColoring(),
    @FloatRange(0.0, 1.0) val backgroundOpacity: Float = 1f,
    val fontSize: FontSize = FontSize.Medium,
    val propertyValueAlignment: Alignment = Alignment.Left
)

