package com.w2sv.domain.model.widget

import androidx.annotation.ColorInt
import androidx.annotation.StringRes
import com.w2sv.androidutils.os.dynamicColorsSupported
import com.w2sv.core.domain.R
import com.w2sv.domain.model.Theme

sealed interface WidgetColoring {

    val asPresetOrNull: Preset?
        get() = this as? Preset

    data class Preset(
        val theme: Theme = Theme.Default,
        val useDynamicColors: Boolean = dynamicColorsSupported
    ) : WidgetColoring

    data class Custom(
        @ColorInt val background: Int = -7859146,
        @ColorInt val primary: Int = -5898336,
        @ColorInt val secondary: Int = -1
    ) : WidgetColoring

    @get:StringRes
    val labelRes: Int
        get() = when (this) {
            is Preset -> R.string.preset
            is Custom -> R.string.custom
        }
}
