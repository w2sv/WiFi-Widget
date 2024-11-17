package com.w2sv.widget.data

import com.w2sv.domain.repository.WidgetRepository
import com.w2sv.kotlinutils.coroutines.flow.firstBlocking
import com.w2sv.kotlinutils.coroutines.flow.mapValuesToFirstBlocking
import com.w2sv.widget.model.WidgetAppearance
import com.w2sv.widget.model.WidgetBottomBarElement
import com.w2sv.widget.model.WidgetRefreshing

internal val WidgetRepository.appearanceBlocking: WidgetAppearance
    get() = WidgetAppearance(
        coloringConfig = coloringConfig.firstBlocking(),
        backgroundOpacity = opacity.firstBlocking(),
        fontSize = fontSize.firstBlocking(),
        bottomRow = bottomRowBlocking
    )

private val WidgetRepository.bottomRowBlocking: WidgetBottomBarElement
    get() = bottomRowElementEnablementMap
        .mapValuesToFirstBlocking()
        .run { WidgetBottomBarElement(this) }

internal val WidgetRepository.refreshingBlocking: WidgetRefreshing
    get() = refreshingParametersEnablementMap
        .mapValuesToFirstBlocking()
        .run { WidgetRefreshing(parameters = this, interval = refreshInterval.firstBlocking()) }
