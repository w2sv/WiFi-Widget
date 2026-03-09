package com.w2sv.wifiwidget.ui.screen.widgetconfig.model

import androidx.annotation.ColorInt
import androidx.annotation.StringRes
import com.w2sv.domain.model.widget.WidgetColoringStrategy

operator fun WidgetColoringStrategy.Custom.get(color: WidgetColor): Int =
    when (color) {
        WidgetColor.Background -> colors.background
        WidgetColor.Primary -> colors.primary
        WidgetColor.Secondary -> colors.secondary
    }

fun WidgetColoringStrategy.Custom.set(color: WidgetColor, @ColorInt value: Int): WidgetColoringStrategy.Custom =
    WidgetColoringStrategy.Custom(
        when (color) {
            WidgetColor.Background -> colors.copy(background = value)
            WidgetColor.Primary -> colors.copy(primary = value)
            WidgetColor.Secondary -> colors.copy(secondary = value)
        }
    )

@get:StringRes
val WidgetColoringStrategy.labelRes: Int
    get() = when (this) {
        is Preset -> com.w2sv.core.domain.R.string.theme
        is Custom -> com.w2sv.core.domain.R.string.custom
    }
