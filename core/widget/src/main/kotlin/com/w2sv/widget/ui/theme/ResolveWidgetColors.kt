package com.w2sv.widget.ui.theme

import android.content.Context
import androidx.annotation.ColorRes
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.ui.graphics.toArgb
import com.w2sv.androidutils.os.dynamicColorsSupported
import com.w2sv.androidutils.res.isNightModeActiveCompat
import com.w2sv.core.widget.R
import com.w2sv.domain.preferences.Theme
import com.w2sv.domain.widget.WidgetColoring
import com.w2sv.domain.widget.WidgetColoringStrategy
import com.w2sv.domain.widget.WidgetColors

internal fun WidgetColoring.resolve(context: Context): WidgetColors =
    when (val style = appliedStrategy) {
        is WidgetColoringStrategy.Custom -> style.colors
        is WidgetColoringStrategy.Preset -> {
            val dark = when (style.theme) {
                Theme.Light -> false
                Theme.Dark -> true
                Theme.Default -> context.resources.configuration.isNightModeActiveCompat
            }
            if (style.useDynamicColors && dynamicColorsSupported) {
                val scheme = if (dark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
                WidgetColors(
                    background = scheme.surface.toArgb(),
                    primary = scheme.primary.toArgb(),
                    secondary = scheme.onSurface.toArgb()
                )
            } else {
                context.staticColors(dark)
            }
        }
    }

private fun Context.staticColors(dark: Boolean): WidgetColors =
    if (dark) {
        widgetColors(
            background = R.color.background_dark,
            primary = R.color.default_label,
            secondary = R.color.foreground_dark
        )
    } else {
        widgetColors(
            background = R.color.background_light,
            primary = R.color.label_light,
            secondary = R.color.foreground_light
        )
    }

private fun Context.widgetColors(
    @ColorRes background: Int,
    @ColorRes primary: Int,
    @ColorRes secondary: Int
): WidgetColors =
    WidgetColors(
        background = getColor(background),
        primary = getColor(primary),
        secondary = getColor(secondary)
    )
