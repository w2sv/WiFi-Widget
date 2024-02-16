package com.w2sv.widget.data

import com.w2sv.domain.model.WidgetBottomRowElement
import com.w2sv.domain.model.WidgetColoring
import com.w2sv.domain.model.WidgetRefreshingParameter
import com.w2sv.domain.repository.WidgetRepository
import com.w2sv.widget.model.WidgetAppearance
import com.w2sv.widget.model.WidgetBottomRow
import com.w2sv.widget.model.WidgetRefreshing

val WidgetRepository.appearance: WidgetAppearance
    get() = WidgetAppearance(
        coloring = when (coloring.value) {
            WidgetColoring.Preset -> presetColoringData.value
            WidgetColoring.Custom -> customColoringData.value
        },
        backgroundOpacity = opacity.value,
        bottomRow = bottomRow,
    )

private val WidgetRepository.bottomRow: WidgetBottomRow
    get() = WidgetBottomRow(
        lastRefreshTimeDisplay = bottomRowElementEnablementMap.getValue(WidgetBottomRowElement.LastRefreshTimeDisplay).value,
        refreshButton = bottomRowElementEnablementMap.getValue(WidgetBottomRowElement.RefreshButton).value,
        goToWifiSettingsButton = bottomRowElementEnablementMap.getValue(WidgetBottomRowElement.GoToWifiSettingsButton).value,
        goToWidgetSettingsButton = bottomRowElementEnablementMap.getValue(WidgetBottomRowElement.GoToWidgetSettingsButton).value
    )

val WidgetRepository.refreshing: WidgetRefreshing
    get() = WidgetRefreshing(
        refreshPeriodically = refreshingParametersEnablementMap.getValue(WidgetRefreshingParameter.RefreshPeriodically).value,
        refreshOnLowBattery = refreshingParametersEnablementMap.getValue(WidgetRefreshingParameter.RefreshOnLowBattery).value
    )