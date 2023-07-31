package com.w2sv.common.data.model

import android.content.Context
import androidx.annotation.ColorInt
import androidx.annotation.FloatRange
import androidx.core.graphics.ColorUtils
import com.w2sv.common.R
import com.w2sv.common.extensions.isNightModeActiveCompat
import com.w2sv.common.extensions.toRGBChannelInt

data class WidgetAppearance(
    val theme: WidgetTheme,
    @FloatRange(0.0, 1.0) val opacity: Float,
    val displayLastRefreshDateTime: Boolean
) {
    fun getBackgroundOpacityIntegratedColors(context: Context): WidgetColors {
        val rawColors = theme.getColors(context)

        return WidgetColors(
            background = ColorUtils.setAlphaComponent(
                rawColors.background,
                opacity.toRGBChannelInt()
            ),
            labels = rawColors.labels,
            other = rawColors.other
        )
    }
}

sealed interface WidgetTheme {
    fun getColors(context: Context): WidgetColors

    sealed class Static : WidgetTheme {
        abstract val backgroundRes: Int
        abstract val foregroundRes: Int
        private val labelsRes: Int = R.color.default_label

        override fun getColors(context: Context): WidgetColors =
            WidgetColors(
                background = context.getColor(backgroundRes),
                labels = context.getColor(labelsRes),
                other = context.getColor(foregroundRes)
            )
    }

    object Light : Static() {
        override val backgroundRes: Int = R.color.background_light
        override val foregroundRes: Int = R.color.foreground_light
    }

    object Dark : Static() {
        override val backgroundRes: Int = R.color.background_dark
        override val foregroundRes: Int = R.color.foreground_dark
    }

    object DeviceDefault : WidgetTheme {
        override fun getColors(context: Context): WidgetColors =
            when (context.resources.configuration.isNightModeActiveCompat) {
                false -> Light
                true -> Dark
            }
                .getColors(context)
    }

    data class Custom(val colors: WidgetColors) : WidgetTheme {
        override fun getColors(context: Context): WidgetColors = colors
    }
}

data class WidgetColors(
    @ColorInt val background: Int,
    @ColorInt val labels: Int,
    @ColorInt val other: Int
)
