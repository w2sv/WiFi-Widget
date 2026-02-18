package com.w2sv.domain.model.widget

data class WidgetColoringConfig(
    val preset: WidgetColoring.Preset = WidgetColoring.Preset(),
    val custom: WidgetColoring.Custom = WidgetColoring.Custom(),
    val isCustomSelected: Boolean = false
) {
    val styles: List<WidgetColoring>
        get() = listOf(preset, custom)

    val appliedStyle: WidgetColoring
        get() = if (isCustomSelected) custom else preset
}
