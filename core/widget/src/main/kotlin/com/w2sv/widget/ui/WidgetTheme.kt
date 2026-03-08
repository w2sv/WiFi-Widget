package com.w2sv.widget.ui

import android.annotation.SuppressLint
import android.content.Context
import androidx.annotation.ColorRes
import androidx.annotation.StyleRes
import androidx.core.content.res.use
import com.google.android.material.R
import com.google.android.material.color.DynamicColors
import com.w2sv.androidutils.res.isNightModeActiveCompat
import com.w2sv.domain.model.widget.WidgetColors

internal enum class WidgetTheme(@StyleRes private val dynamicThemeRes: Int, private val makePresetColors: Context.() -> WidgetColors) {
    Light(
        R.style.Theme_Material3_DynamicColors_Light,
        {
            presetColors(
                com.w2sv.core.widget.R.color.background_light,
                com.w2sv.core.widget.R.color.default_label,
                com.w2sv.core.widget.R.color.foreground_light
            )
        }
    ),

    Dark(
        R.style.Theme_Material3_DynamicColors_Dark,
        {
            presetColors(
                com.w2sv.core.widget.R.color.background_dark,
                com.w2sv.core.widget.R.color.default_label,
                com.w2sv.core.widget.R.color.foreground_dark
            )
        }
    );

    fun resolve(context: Context, useDynamicColors: Boolean): WidgetColors =
        if (useDynamicColors) {
            dynamicWidgetColors(context, dynamicThemeRes)
        } else {
            makePresetColors(context)
        }

    companion object {
        fun systemDefault(context: Context): WidgetTheme =
            if (context.resources.configuration.isNightModeActiveCompat) Dark else Light
    }
}

private fun Context.presetColors(
    @ColorRes bgRes: Int,
    @ColorRes primaryRes: Int,
    @ColorRes secondaryRes: Int
): WidgetColors =
    WidgetColors(
        background = getColor(bgRes),
        primary = getColor(primaryRes),
        secondary = getColor(secondaryRes)
    )

@SuppressLint("ResourceType")
private fun dynamicWidgetColors(context: Context, @StyleRes wrapperTheme: Int): WidgetColors =
    DynamicColors.wrapContextIfAvailable(
        context,
        wrapperTheme
    )
        .obtainStyledAttributes(
            intArrayOf(
                R.attr.colorSurface,
                R.attr.colorPrimaryFixed,
                R.attr.colorSecondary
            )
        )
        .use {
            WidgetColors(
                background = it.getColor(0, 0),
                primary = it.getColor(1, 0),
                secondary = it.getColor(2, 0)
            )
        }
