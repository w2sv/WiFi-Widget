package com.w2sv.widget.model

import com.w2sv.domain.model.WidgetBottomRowElement

internal data class WidgetBottomBarElement(
    val lastRefreshTimeDisplay: Boolean,
    val refreshButton: Boolean,
    val goToWifiSettingsButton: Boolean,
    val goToWidgetSettingsButton: Boolean,
) {
    constructor(parameters: Map<WidgetBottomRowElement, Boolean>) : this(
        lastRefreshTimeDisplay = parameters.getValue(WidgetBottomRowElement.LastRefreshTimeDisplay),
        refreshButton = parameters.getValue(WidgetBottomRowElement.RefreshButton),
        goToWifiSettingsButton = parameters.getValue(WidgetBottomRowElement.GoToWifiSettingsButton),
        goToWidgetSettingsButton = parameters.getValue(WidgetBottomRowElement.GoToWidgetSettingsButton)
    )

    val anyEnabled: Boolean
        get() = listOf(
            lastRefreshTimeDisplay,
            refreshButton,
            goToWifiSettingsButton,
            goToWidgetSettingsButton
        )
            .any { it }
}
