package com.w2sv.widget.data

import com.w2sv.androidutils.coroutines.getSynchronousMap
import com.w2sv.androidutils.coroutines.getValueSynchronously
import com.w2sv.domain.model.WidgetBottomRowElement
import com.w2sv.domain.repository.WidgetRepository
import com.w2sv.widget.model.WidgetAppearance
import com.w2sv.widget.model.WidgetBottomRow
import com.w2sv.widget.model.WidgetRefreshing

val WidgetRepository.appearanceBlocking: WidgetAppearance
    get() = WidgetAppearance(
        coloringConfig = coloringConfig.getValueSynchronously(),
        backgroundOpacity = opacity.getValueSynchronously(),
        fontSize = fontSize.getValueSynchronously(),
        bottomRow = bottomRowBlocking,
    )

private val WidgetRepository.bottomRowBlocking: WidgetBottomRow
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

val WidgetRepository.refreshingBlocking: WidgetRefreshing
    get() = refreshingParametersEnablementMap
        .getSynchronousMap()
        .run {
            WidgetRefreshing(this)
        }