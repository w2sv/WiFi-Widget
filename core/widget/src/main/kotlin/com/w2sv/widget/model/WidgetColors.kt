package com.w2sv.widget.model

import android.annotation.SuppressLint
import android.content.Context
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.StyleRes
import androidx.core.content.res.use
import com.google.android.material.color.DynamicColors
import com.w2sv.androidutils.graphics.getAlphaSetColor
import com.w2sv.androidutils.res.isNightModeActiveCompat
import com.w2sv.core.common.R
import com.w2sv.domain.model.Theme
import com.w2sv.domain.model.WidgetColoring

internal data class WidgetColors(
    @param:ColorInt val background: Int,
    @param:ColorInt val primary: Int,
    @param:ColorInt val secondary: Int
) {
    data class Resources(@param:ColorRes val background: Int, @param:ColorRes val primary: Int, @param:ColorRes val secondary: Int) {
        fun getColors(context: Context): WidgetColors =
            WidgetColors(
                context.getColor(background),
                context.getColor(primary),
                context.getColor(secondary)
            )
    }

    val ipSubPropertyBackgroundColor by lazy {
        getAlphaSetColor(primary, 0.25f)
    }

    companion object {
        fun fromStyle(style: WidgetColoring.Style, context: Context): WidgetColors =
            when (style) {
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
}

private sealed interface WidgetTheme {

    fun getColors(context: Context, useDynamicColors: Boolean): WidgetColors

    sealed class DayOrNight(@param:StyleRes private val dynamicWrapperTheme: Int, private val nonDynamicResources: WidgetColors.Resources) :
        WidgetTheme {

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
            com.w2sv.core.widget.R.color.background_light,
            com.w2sv.core.widget.R.color.default_label,
            com.w2sv.core.widget.R.color.foreground_light
        )
    )

    data object Dark : DayOrNight(
        com.google.android.material.R.style.Theme_Material3_DynamicColors_Dark,
        WidgetColors.Resources(
            com.w2sv.core.widget.R.color.background_dark,
            com.w2sv.core.widget.R.color.default_label,
            com.w2sv.core.widget.R.color.foreground_dark
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
