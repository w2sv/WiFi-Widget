package com.w2sv.domain.model

import androidx.annotation.ColorInt
import androidx.annotation.StringRes
import com.w2sv.common.utils.dynamicColorsSupported
import com.w2sv.core.domain.R

enum class WidgetColoring(@StringRes val labelRes: Int) {
    Preset(R.string.preset),
    Custom(R.string.custom);

    sealed interface Data {
        data class Preset(
            val theme: Theme = Defaults.THEME,
            val useDynamicColors: Boolean = Defaults.USE_DYNAMIC_COLORS
        ) : Data {

            object Defaults {
                val THEME = Theme.SystemDefault
                val USE_DYNAMIC_COLORS = dynamicColorsSupported
            }
        }

        data class Custom(
            @ColorInt val background: Int = Defaults.BACKGROUND,
            @ColorInt val primary: Int = Defaults.PRIMARY,
            @ColorInt val secondary: Int = Defaults.SECONDARY
        ) : List<Int> by listOf(background, primary, secondary),
            Data {

            object Defaults {
                const val BACKGROUND = -7859146
                const val PRIMARY = -5898336
                const val SECONDARY = -1
            }
        }
    }
}