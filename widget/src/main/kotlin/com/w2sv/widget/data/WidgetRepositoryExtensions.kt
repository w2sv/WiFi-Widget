package com.w2sv.widget.data

import com.w2sv.domain.model.Theme
import com.w2sv.domain.model.WidgetBottomRowElement
import com.w2sv.domain.model.WidgetColorSection
import com.w2sv.domain.model.WidgetRefreshingParameter
import com.w2sv.domain.repository.WidgetRepository
import com.w2sv.widget.model.WidgetAppearance
import com.w2sv.widget.model.WidgetBottomRow
import com.w2sv.widget.model.WidgetColors
import com.w2sv.widget.model.WidgetRefreshing
import com.w2sv.widget.model.WidgetTheme

val WidgetRepository.appearance: WidgetAppearance
    get() = WidgetAppearance(
        useDynamicColors = useDynamicColors.value,
        theme = when (theme.value) {
            Theme.Light -> WidgetTheme.Light
            Theme.SystemDefault -> WidgetTheme.DeviceDefault
            Theme.Dark -> WidgetTheme.Dark
            Theme.Custom -> WidgetTheme.Custom(
                customColors
            )
        },
        backgroundOpacity = opacity.value,
        bottomRow = bottomRow,
    )

private val WidgetRepository.customColors: WidgetColors
    get() = WidgetColors(
        background = customColorsMap.getValue(WidgetColorSection.Background).value,
        primary = customColorsMap.getValue(WidgetColorSection.Primary).value,
        secondary = customColorsMap.getValue(WidgetColorSection.Secondary).value
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