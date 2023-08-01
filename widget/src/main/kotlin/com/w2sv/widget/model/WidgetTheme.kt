package com.w2sv.widget.model

import android.annotation.SuppressLint
import android.content.Context
import com.google.android.material.color.DynamicColors
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
                primary = context.getColor(labelsRes),
                secondary = context.getColor(foregroundRes)
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
            if (DynamicColors.isDynamicColorAvailable()) {
                DynamicColors.wrapContextIfAvailable(
                    context,
                    com.google.android.material.R.style.Theme_Material3_DynamicColors_DayNight
                )
                    .obtainStyledAttributes(
                        intArrayOf(
                            com.google.android.material.R.attr.colorSurface,
                            com.google.android.material.R.attr.colorPrimary,
                            com.google.android.material.R.attr.colorSecondary
                        )
                    )
                    .run {
                        @SuppressLint("ResourceType")
                        val colors = WidgetColors(
                            background = getColor(0, 0),
                            primary = getColor(1, 0),
                            secondary = getColor(2, 0)
                        )
                        recycle()
                        colors
                    }
            } else {
                (if (context.resources.configuration.isNightModeActiveCompat) Dark else Light)
                    .getColors(
                        context
                    )
            }
    }

    data class Custom(val colors: WidgetColors) : WidgetTheme {
        override fun getColors(context: Context): WidgetColors = colors
    }
}