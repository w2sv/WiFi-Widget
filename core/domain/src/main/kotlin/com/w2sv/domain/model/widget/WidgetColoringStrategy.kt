package com.w2sv.domain.model.widget

import com.w2sv.androidutils.os.dynamicColorsSupported
import com.w2sv.domain.model.Theme

private val defaultCustomColors = WidgetColors(-7859146, -5898336, -1)

sealed interface WidgetColoringStrategy {

    val isCustom get() = this is Custom

    data class Preset(val theme: Theme = Theme.Default, val useDynamicColors: Boolean = dynamicColorsSupported) : WidgetColoringStrategy

    @JvmInline
    value class Custom(val colors: WidgetColors = defaultCustomColors) : WidgetColoringStrategy
}
