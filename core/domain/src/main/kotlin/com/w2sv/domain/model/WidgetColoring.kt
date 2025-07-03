package com.w2sv.domain.model

import androidx.annotation.ColorInt
import androidx.annotation.StringRes
import com.w2sv.androidutils.os.dynamicColorsSupported
import com.w2sv.core.domain.R

interface WidgetColoring {

    data class Config(
        val preset: Style.Preset = Style.Preset(),
        val custom: Style.Custom = Style.Custom(),
        val isCustomSelected: Boolean = false
    ) {
        val styles: List<Style>
            get() = listOf(preset, custom)

        val appliedStyle: Style
            get() = if (isCustomSelected) custom else preset
    }

    sealed interface Style {

        val asPresetOrNull: Preset?
            get() = this as? Preset

        data class Preset(
            val theme: Theme = Theme.Default,
            val useDynamicColors: Boolean = dynamicColorsSupported
        ) : Style

        data class Custom(
            @all:ColorInt val background: Int = -7859146,
            @all:ColorInt val primary: Int = -5898336,
            @all:ColorInt val secondary: Int = -1
        ) : Style

        @get:StringRes
        val labelRes: Int
            get() = when (this) {
                is Preset -> R.string.preset
                is Custom -> R.string.custom
            }
    }
}
