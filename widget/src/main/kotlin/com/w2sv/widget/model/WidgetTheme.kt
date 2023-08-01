package com.w2sv.widget.model

import android.content.Context
import com.w2sv.common.extensions.isNightModeActiveCompat
import com.w2sv.widget.R

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