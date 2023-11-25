package com.w2sv.widget.data

import com.w2sv.data.repositories.WidgetRepository
import com.w2sv.domain.model.Theme
import com.w2sv.domain.model.WidgetButton
import com.w2sv.domain.model.WidgetColor
import com.w2sv.domain.model.WidgetRefreshingParameter
import com.w2sv.widget.model.WidgetAppearance
import com.w2sv.widget.model.WidgetButtons
import com.w2sv.widget.model.WidgetColors
import com.w2sv.widget.model.WidgetRefreshing
import com.w2sv.widget.model.WidgetTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

val WidgetRepository.appearance: Flow<WidgetAppearance>
    get() = combine(
        combine(
            useDynamicColors,
            theme,
            customColors,
            transform = { a, b, c -> Triple(a, b, c) },
        ),
        opacity,
        getRefreshingParametersEnablementMap().getValue(WidgetRefreshingParameter.DisplayLastRefreshDateTime),
        buttons,
        transform = { (useDynamicColors, theme, customColors), opacity, displayLastRefreshDateTime, buttons ->
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
                displayLastRefreshDateTime = displayLastRefreshDateTime,
                buttons = buttons,
            )
        },
    )

val WidgetRepository.buttons: Flow<WidgetButtons>
    get() = combine(
        getButtonEnablementMap().getValue(WidgetButton.Refresh),
        getButtonEnablementMap().getValue(WidgetButton.GoToWifiSettings),
        getButtonEnablementMap().getValue(WidgetButton.GoToWidgetSettings),
        transform = { a, b, c ->
            WidgetButtons(refresh = a, goToWifiSettings = b, goToWidgetSettings = c)
        },
    )

val WidgetRepository.customColors: Flow<WidgetColors>
    get() = combine(
        getCustomColorsMap().getValue(WidgetColor.Background),
        getCustomColorsMap().getValue(WidgetColor.Primary),
        getCustomColorsMap().getValue(WidgetColor.Secondary),
        transform = { background, labels, other ->
            WidgetColors(background, labels, other)
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
