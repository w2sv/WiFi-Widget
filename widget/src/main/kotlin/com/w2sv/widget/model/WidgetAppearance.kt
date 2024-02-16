package com.w2sv.widget.model

import android.content.Context
import androidx.annotation.FloatRange
import com.w2sv.domain.model.Theme
import com.w2sv.domain.model.WidgetColoring

data class WidgetAppearance(
    val coloring: WidgetColoring.Data,
    @FloatRange(0.0, 1.0) val backgroundOpacity: Float,
    val bottomRow: WidgetBottomRow,
) {
    fun getColors(context: Context): WidgetColors =
        when (coloring) {
            is WidgetColoring.Data.Preset -> {
                when (coloring.theme) {
                    Theme.Dark -> WidgetTheme.Dark
                    Theme.SystemDefault -> WidgetTheme.SystemDefault
                    Theme.Light -> WidgetTheme.Light
                }
                    .getColors(context, coloring.useDynamicColors)
            }

            is WidgetColoring.Data.Custom -> {
                WidgetColors(
                    background = coloring.background,
                    primary = coloring.primary,
                    secondary = coloring.secondary,
                )
            }
        }
}
