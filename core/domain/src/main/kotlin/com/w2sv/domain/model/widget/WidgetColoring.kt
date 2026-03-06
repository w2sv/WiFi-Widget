package com.w2sv.domain.model.widget

data class WidgetColoring(
    val preset: WidgetColoringStrategy.Preset = WidgetColoringStrategy.Preset(),
    val custom: WidgetColoringStrategy.Custom = WidgetColoringStrategy.Custom(),
    val useCustom: Boolean = false
) {
    val strategies: List<WidgetColoringStrategy>
        get() = listOf(preset, custom)

    val appliedStrategy: WidgetColoringStrategy
        get() = if (useCustom) custom else preset
}
