package com.w2sv.domain.model

import androidx.annotation.ColorInt
import androidx.annotation.StringRes
import com.w2sv.common.utils.dynamicColorsSupported
import com.w2sv.core.domain.R

sealed interface WidgetColoring {

    data class Preset(
        val theme: Theme = Theme.SystemDefault,
        val useDynamicColors: Boolean = dynamicColorsSupported
    ) : WidgetColoring

    data class Custom(
        @ColorInt val background: Int = -7859146,
        @ColorInt val primary: Int = -5898336,
        @ColorInt val secondary: Int = -1
    ) : WidgetColoring

    @get:StringRes
    val label: Int
        get() = when (this) {
            is Preset -> R.string.preset
            is Custom -> R.string.custom
        }

    data class Config(
        val preset: Preset = Preset(),
        val custom: Custom = Custom(),
        val isCustomSelected: Boolean = false
    ) {
        val selected: WidgetColoring
            get() = if (isCustomSelected) custom else preset
    }
}
