package com.w2sv.widget.model

import com.w2sv.domain.model.WidgetBottomBarElement

internal data class WidgetBottomBarElement(
    val lastRefreshTimeDisplay: Boolean,
    val refreshButton: Boolean,
    val goToWifiSettingsButton: Boolean,
    val goToWidgetSettingsButton: Boolean
) {
    constructor(parameters: Map<WidgetBottomBarElement, Boolean>) : this(
        lastRefreshTimeDisplay = parameters.getValue(WidgetBottomBarElement.LastRefreshTimeDisplay),
        refreshButton = parameters.getValue(WidgetBottomBarElement.RefreshButton),
        goToWifiSettingsButton = parameters.getValue(WidgetBottomBarElement.GoToWifiSettingsButton),
        goToWidgetSettingsButton = parameters.getValue(WidgetBottomBarElement.GoToWidgetSettingsButton)
    )

    val isAnyEnabled: Boolean
        get() = listOf(
            lastRefreshTimeDisplay,
            refreshButton,
            goToWifiSettingsButton,
            goToWidgetSettingsButton
        )
            .any { it }
}
