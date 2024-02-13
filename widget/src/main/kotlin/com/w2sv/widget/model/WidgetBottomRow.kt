package com.w2sv.widget.model

data class WidgetBottomRow(
    val lastRefreshTimeDisplay: Boolean,
    val refreshButton: Boolean,
    val goToWifiSettingsButton: Boolean,
    val goToWidgetSettingsButton: Boolean,
) : List<Boolean> by listOf(
    lastRefreshTimeDisplay,
    refreshButton,
    goToWifiSettingsButton,
    goToWidgetSettingsButton
)
