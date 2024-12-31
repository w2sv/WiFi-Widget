package com.w2sv.widget.model

import androidx.annotation.FloatRange
import com.w2sv.domain.model.FontSize
import com.w2sv.domain.model.PropertyValueAlignment
import com.w2sv.domain.model.WidgetColoring

internal data class WidgetAppearance(
    val coloringConfig: WidgetColoring.Config,
    @FloatRange(0.0, 1.0) val backgroundOpacity: Float,
    val fontSize: FontSize,
    val propertyValueAlignment: PropertyValueAlignment,
    val bottomBar: WidgetBottomBarElement
)
