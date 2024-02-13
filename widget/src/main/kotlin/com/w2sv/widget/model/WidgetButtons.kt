package com.w2sv.widget.model

data class WidgetButtons(
    val refresh: Boolean,
    val goToWifiSettings: Boolean,
    val goToWidgetSettings: Boolean,
) : List<Boolean> by listOf(refresh, goToWifiSettings, goToWidgetSettings)
