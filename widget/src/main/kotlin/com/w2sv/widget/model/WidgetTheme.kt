package com.w2sv.widget.model

import android.content.Context
import com.w2sv.widget.R

sealed interface WidgetTheme {

    sealed interface ManualColorSetting {
        fun getColors(context: Context): WidgetColors
    }

    sealed class Static : WidgetTheme, ManualColorSetting {
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

    object DeviceDefault : WidgetTheme

    data class Custom(val colors: WidgetColors) : WidgetTheme, ManualColorSetting {
        override fun getColors(context: Context): WidgetColors = colors
    }
}