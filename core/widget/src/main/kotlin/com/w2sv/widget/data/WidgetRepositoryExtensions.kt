package com.w2sv.widget.data

import com.w2sv.androidutils.coroutines.getSynchronousMap
import com.w2sv.androidutils.coroutines.getValueSynchronously
import com.w2sv.domain.model.WidgetBottomRowElement
import com.w2sv.domain.model.WidgetRefreshingParameter
import com.w2sv.domain.repository.WidgetRepository
import com.w2sv.widget.model.WidgetAppearance
import com.w2sv.widget.model.WidgetBottomRow
import com.w2sv.widget.model.WidgetRefreshing

val WidgetRepository.appearance: WidgetAppearance
    get() = WidgetAppearance(
        coloringConfig = coloringConfig.getValueSynchronously(),
        backgroundOpacity = opacity.getValueSynchronously(),
        fontSize = fontSize.getValueSynchronously(),
        bottomRow = bottomRow,
    )

private val WidgetRepository.bottomRow: WidgetBottomRow
    get() = bottomRowElementEnablementMap
        .getSynchronousMap()
        .run {
            WidgetBottomRow(
                lastRefreshTimeDisplay = getValue(WidgetBottomRowElement.LastRefreshTimeDisplay),
                refreshButton = getValue(WidgetBottomRowElement.RefreshButton),
                goToWifiSettingsButton = getValue(WidgetBottomRowElement.GoToWifiSettingsButton),
                goToWidgetSettingsButton = getValue(WidgetBottomRowElement.GoToWidgetSettingsButton)
            )
        }

val WidgetRepository.refreshing: WidgetRefreshing
    get() = refreshingParametersEnablementMap.getSynchronousMap().run {
        WidgetRefreshing(
            refreshPeriodically = getValue(WidgetRefreshingParameter.RefreshPeriodically),
            refreshOnLowBattery = getValue(WidgetRefreshingParameter.RefreshOnLowBattery)
        )
    }