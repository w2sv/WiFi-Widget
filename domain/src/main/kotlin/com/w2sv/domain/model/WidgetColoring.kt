package com.w2sv.domain.model

import androidx.annotation.ColorInt
import androidx.annotation.StringRes
import com.w2sv.common.utils.dynamicColorsSupported
import com.w2sv.domain.R

enum class WidgetColoring(@StringRes val labelRes: Int) {
    Preset(R.string.preset),
    Custom(R.string.custom);

    sealed interface Data {
        data class Preset(
            val theme: Theme = Theme.SystemDefault,
            val useDynamicColors: Boolean = dynamicColorsSupported
        ) : Data

        data class Custom(
            @ColorInt val background: Int = -7859146,
            @ColorInt val primary: Int = -5898336,
            @ColorInt val secondary: Int = -1
        ) : List<Int> by listOf(background, primary, secondary),
            Data
    }
}