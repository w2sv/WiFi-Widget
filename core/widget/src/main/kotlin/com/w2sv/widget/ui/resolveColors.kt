package com.w2sv.widget.ui

import android.content.Context
import com.w2sv.domain.preferences.Theme
import com.w2sv.domain.widget.WidgetColoring
import com.w2sv.domain.widget.WidgetColoringStrategy
import com.w2sv.domain.widget.WidgetColors

internal fun WidgetColoring.resolve(context: Context): WidgetColors =
    when (val style = appliedStrategy) {
        is WidgetColoringStrategy.Preset -> {
            when (style.theme) {
                Theme.Dark -> WidgetTheme.Dark
                Theme.Light -> WidgetTheme.Light
                Theme.Default -> WidgetTheme.systemDefault(context)
            }
                .resolve(context, style.useDynamicColors)
        }

        is WidgetColoringStrategy.Custom -> style.colors
    }
