package com.w2sv.widget.data

import com.w2sv.domain.model.Theme
import com.w2sv.domain.model.WidgetBottomBarElement
import com.w2sv.domain.model.WidgetColorSection
import com.w2sv.domain.model.WidgetRefreshingParameter
import com.w2sv.domain.repository.WidgetRepository
import com.w2sv.widget.model.WidgetAppearance
import com.w2sv.widget.model.WidgetBottomBar
import com.w2sv.widget.model.WidgetColors
import com.w2sv.widget.model.WidgetRefreshing
import com.w2sv.widget.model.WidgetTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

val WidgetRepository.appearance: Flow<WidgetAppearance>
    get() = combine(
        useDynamicColors,
        theme,
        customColors,
        opacity,
        bottomBar,
        transform = { useDynamicColors, theme, customColors, opacity, bottomBar ->
            WidgetAppearance(
                useDynamicColors = useDynamicColors,
                theme = when (theme) {
                    Theme.Light -> WidgetTheme.Light
                    Theme.SystemDefault -> WidgetTheme.DeviceDefault
                    Theme.Dark -> WidgetTheme.Dark
                    Theme.Custom -> WidgetTheme.Custom(
                        customColors,
                    )
                },
                backgroundOpacity = opacity,
                bottomBar = bottomBar,
            )
        },
    )

private val WidgetRepository.customColors: Flow<WidgetColors>
    get() = combine(
        getCustomColorsMap().getValue(WidgetColorSection.Background),
        getCustomColorsMap().getValue(WidgetColorSection.Primary),
        getCustomColorsMap().getValue(WidgetColorSection.Secondary),
        transform = { background, labels, other ->
            WidgetColors(background, labels, other)
        },
    )

private val WidgetRepository.bottomBar: Flow<WidgetBottomBar>
    get() = combine(
        getButtonEnablementMap().getValue(WidgetBottomBarElement.LastRefreshTimeDisplay),
        getButtonEnablementMap().getValue(WidgetBottomBarElement.RefreshButton),
        getButtonEnablementMap().getValue(WidgetBottomBarElement.GoToWifiSettingsButton),
        getButtonEnablementMap().getValue(WidgetBottomBarElement.GoToWidgetSettingsButton),
        transform = { lastRefreshTimeDisplay, refreshButton, goToWifiSettingsButton, goToWidgetSettingsButton ->
            WidgetBottomBar(
                lastRefreshTimeDisplay = lastRefreshTimeDisplay,
                refreshButton = refreshButton,
                goToWifiSettingsButton = goToWifiSettingsButton,
                goToWidgetSettingsButton = goToWidgetSettingsButton
            )
        },
    )

val WidgetRepository.refreshing: Flow<WidgetRefreshing>
    get() = combine(
        getRefreshingParametersEnablementMap().getValue(WidgetRefreshingParameter.RefreshPeriodically),
        getRefreshingParametersEnablementMap().getValue(WidgetRefreshingParameter.RefreshOnLowBattery),
        transform = { refreshPeriodically, refreshOnLowBattery ->
            WidgetRefreshing(
                refreshPeriodically = refreshPeriodically,
                refreshOnLowBattery = refreshOnLowBattery,
            )
        },
    )
