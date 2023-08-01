package com.w2sv.widget.data

import com.w2sv.data.model.Theme
import com.w2sv.data.model.WidgetColor
import com.w2sv.data.model.WidgetRefreshingParameter
import com.w2sv.data.storage.WidgetRepository
import com.w2sv.widget.model.WidgetAppearance
import com.w2sv.widget.model.WidgetColors
import com.w2sv.widget.model.WidgetRefreshing
import com.w2sv.widget.model.WidgetTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

val WidgetRepository.appearance: Flow<WidgetAppearance>
    get() = combine(
        theme,
        customColors,
        opacity,
        refreshingParametersMap.getValue(WidgetRefreshingParameter.DisplayLastRefreshDateTime),
        transform = { theme, customColors, opacity, displayLastRefreshDateTime ->
            WidgetAppearance(
                theme = when (theme) {
                    Theme.Light -> WidgetTheme.Light
                    Theme.DeviceDefault -> WidgetTheme.DeviceDefault
                    Theme.Dark -> WidgetTheme.Dark
                    Theme.Custom -> WidgetTheme.Custom(
                        customColors
                    )
                },
                backgroundOpacity = opacity,
                displayLastRefreshDateTime = displayLastRefreshDateTime
            )
        }
    )

val WidgetRepository.customColors: Flow<WidgetColors>
    get() = combine(
        customColorsMap.getValue(WidgetColor.Background),
        customColorsMap.getValue(WidgetColor.Primary),
        customColorsMap.getValue(WidgetColor.Secondary),
        transform = { background, labels, other ->
            WidgetColors(background, labels, other)
        }
    )

val WidgetRepository.refreshing: Flow<WidgetRefreshing>
    get() = combine(
        refreshingParametersMap.getValue(WidgetRefreshingParameter.RefreshPeriodically),
        refreshingParametersMap.getValue(WidgetRefreshingParameter.RefreshOnLowBattery),
        transform = { refreshPeriodically, refreshOnLowBattery ->
            WidgetRefreshing(
                refreshPeriodically = refreshPeriodically,
                refreshOnLowBattery = refreshOnLowBattery
            )
        }
    )