package com.w2sv.widget.model

import android.annotation.SuppressLint
import android.content.Context
import androidx.annotation.FloatRange
import androidx.annotation.StyleRes
import androidx.core.content.res.use
import com.google.android.material.color.DynamicColors
import com.w2sv.androidutils.generic.isNightModeActiveCompat
import com.w2sv.core.widget.R
import com.w2sv.domain.model.FontSize
import com.w2sv.domain.model.Theme
import com.w2sv.domain.model.WidgetColoring

data class WidgetAppearance(
    val coloring: WidgetColoring,
    @FloatRange(0.0, 1.0) val backgroundOpacity: Float,
    val fontSize: FontSize,
    val bottomRow: WidgetBottomRow,
) {
    fun getColors(context: Context): WidgetColors =
        when (coloring) {
            is WidgetColoring.Preset -> {
                when (coloring.theme) {
                    Theme.Dark -> WidgetTheme.Dark
                    Theme.SystemDefault -> WidgetTheme.SystemDefault
                    Theme.Light -> WidgetTheme.Light
                }
                    .getColors(context, coloring.useDynamicColors)
            }

            is WidgetColoring.Custom -> {
                WidgetColors(
                    background = coloring.background,
                    primary = coloring.primary,
                    secondary = coloring.secondary,
                )
            }
        }
}

private sealed interface WidgetTheme {

    fun getColors(context: Context, useDynamicColors: Boolean): WidgetColors

    sealed class DayOrNight(
        @StyleRes private val dynamicWrapperTheme: Int,
        private val nonDynamicResources: WidgetColors.Resources,
    ) : WidgetTheme {

        override fun getColors(context: Context, useDynamicColors: Boolean): WidgetColors =
            if (useDynamicColors) {
                getDynamicWidgetColors(context, dynamicWrapperTheme)
            } else {
                nonDynamicResources.getColors(context)
            }
    }

    data object Light : DayOrNight(
        com.google.android.material.R.style.Theme_Material3_DynamicColors_Light,
        WidgetColors.Resources(
            R.color.background_light,
            R.color.default_label,
            R.color.foreground_light,
        ),
    )

    data object Dark : DayOrNight(
        com.google.android.material.R.style.Theme_Material3_DynamicColors_Dark,
        WidgetColors.Resources(
            R.color.background_dark,
            R.color.default_label,
            R.color.foreground_dark,
        ),
    )

    data object SystemDefault : WidgetTheme {
        override fun getColors(context: Context, useDynamicColors: Boolean): WidgetColors =
            if (useDynamicColors) {
                getDynamicWidgetColors(
                    context,
                    com.google.android.material.R.style.Theme_Material3_DynamicColors_DayNight,
                )
            } else {
                (if (context.resources.configuration.isNightModeActiveCompat) Dark else Light)
                    .getColors(
                        context,
                        false,
                    )
            }
    }
}

@SuppressLint("ResourceType")
private fun getDynamicWidgetColors(context: Context, @StyleRes wrapperTheme: Int): WidgetColors =
    DynamicColors.wrapContextIfAvailable(
        context,
        wrapperTheme,
    )
        .obtainStyledAttributes(
            intArrayOf(
                com.google.android.material.R.attr.colorSurface,
                com.google.android.material.R.attr.colorPrimary,
                com.google.android.material.R.attr.colorSecondary,
            ),
        )
        .use {
            WidgetColors(
                background = it.getColor(0, 0),
                primary = it.getColor(1, 0),
                secondary = it.getColor(2, 0),
            )
        }
