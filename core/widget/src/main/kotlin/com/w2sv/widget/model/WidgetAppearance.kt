package com.w2sv.widget.model

import android.annotation.SuppressLint
import android.content.Context
import androidx.annotation.FloatRange
import androidx.annotation.StyleRes
import androidx.core.content.res.use
import com.google.android.material.color.DynamicColors
import com.w2sv.androidutils.res.isNightModeActiveCompat
import com.w2sv.core.widget.R
import com.w2sv.domain.model.FontSize
import com.w2sv.domain.model.PropertyValueAlignment
import com.w2sv.domain.model.Theme
import com.w2sv.domain.model.WidgetColoring

internal data class WidgetAppearance(
    val coloringConfig: WidgetColoring.Config,
    @FloatRange(0.0, 1.0) val backgroundOpacity: Float,
    val fontSize: FontSize,
    val propertyValueAlignment: PropertyValueAlignment,
    val bottomBar: WidgetBottomBarElement
) {
    fun getColors(context: Context): WidgetColors =
        when (val style = coloringConfig.appliedStyle) {
            is WidgetColoring.Style.Preset -> {
                when (style.theme) {
                    Theme.Dark -> WidgetTheme.Dark
                    Theme.Default -> WidgetTheme.SystemDefault
                    Theme.Light -> WidgetTheme.Light
                }
                    .getColors(context, style.useDynamicColors)
            }

            is WidgetColoring.Style.Custom -> {
                WidgetColors(
                    background = style.background,
                    primary = style.primary,
                    secondary = style.secondary
                )
            }
        }
}

private sealed interface WidgetTheme {

    fun getColors(context: Context, useDynamicColors: Boolean): WidgetColors

    sealed class DayOrNight(
        @StyleRes private val dynamicWrapperTheme: Int,
        private val nonDynamicResources: WidgetColors.Resources
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
            R.color.foreground_light
        )
    )

    data object Dark : DayOrNight(
        com.google.android.material.R.style.Theme_Material3_DynamicColors_Dark,
        WidgetColors.Resources(
            R.color.background_dark,
            R.color.default_label,
            R.color.foreground_dark
        )
    )

    data object SystemDefault : WidgetTheme {
        override fun getColors(context: Context, useDynamicColors: Boolean): WidgetColors =
            if (useDynamicColors) {
                getDynamicWidgetColors(
                    context,
                    com.google.android.material.R.style.Theme_Material3_DynamicColors_DayNight
                )
            } else {
                (if (context.resources.configuration.isNightModeActiveCompat) Dark else Light)
                    .getColors(
                        context,
                        false
                    )
            }
    }
}

@SuppressLint("ResourceType")
private fun getDynamicWidgetColors(context: Context, @StyleRes wrapperTheme: Int): WidgetColors =
    DynamicColors.wrapContextIfAvailable(
        context,
        wrapperTheme
    )
        .obtainStyledAttributes(
            intArrayOf(
                com.google.android.material.R.attr.colorSurface,
                com.google.android.material.R.attr.colorPrimary,
                com.google.android.material.R.attr.colorSecondary
            )
        )
        .use {
            WidgetColors(
                background = it.getColor(0, 0),
                primary = it.getColor(1, 0),
                secondary = it.getColor(2, 0)
            )
        }
