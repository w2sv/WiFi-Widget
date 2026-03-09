package com.w2sv.domain.model.widget

import com.w2sv.domain.model.Theme

sealed interface WidgetColoringStrategy {

    val isCustom get() = this is Custom

    data class Preset(val theme: Theme, val useDynamicColors: Boolean) : WidgetColoringStrategy

    @JvmInline
    value class Custom(val colors: WidgetColors) : WidgetColoringStrategy
}
