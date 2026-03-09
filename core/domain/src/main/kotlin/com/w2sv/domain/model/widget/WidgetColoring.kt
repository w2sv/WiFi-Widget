package com.w2sv.domain.model.widget

import com.w2sv.androidutils.os.dynamicColorsSupported
import com.w2sv.domain.model.Theme

private val defaultPresetColoring = WidgetColoringStrategy.Preset(
    theme = Theme.Default,
    useDynamicColors = dynamicColorsSupported
)
private val defaultCustomColoring = WidgetColoringStrategy.Custom(
    WidgetColors(
        background = -7859146,
        primary = -5898336,
        secondary = -1
    )
)

data class WidgetColoring(
    val preset: WidgetColoringStrategy.Preset = defaultPresetColoring,
    val custom: WidgetColoringStrategy.Custom = defaultCustomColoring,
    val useCustom: Boolean = false
) {
    val strategies: List<WidgetColoringStrategy>
        get() = listOf(preset, custom)

    val appliedStrategy: WidgetColoringStrategy
        get() = if (useCustom) custom else preset
}
