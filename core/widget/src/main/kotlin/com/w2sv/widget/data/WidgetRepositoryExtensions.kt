package com.w2sv.widget.data

import com.w2sv.androidutils.coroutines.getSynchronousMap
import com.w2sv.androidutils.coroutines.getValueSynchronously
import com.w2sv.domain.repository.WidgetRepository
import com.w2sv.widget.model.WidgetAppearance
import com.w2sv.widget.model.WidgetBottomBarElement
import com.w2sv.widget.model.WidgetRefreshing

internal val WidgetRepository.appearanceBlocking: WidgetAppearance
    get() = WidgetAppearance(
        coloringConfig = coloringConfig.getValueSynchronously(),
        backgroundOpacity = opacity.getValueSynchronously(),
        fontSize = fontSize.getValueSynchronously(),
        bottomRow = bottomRowBlocking,
    )

private val WidgetRepository.bottomRowBlocking: WidgetBottomBarElement
    get() = bottomRowElementEnablementMap
        .getSynchronousMap()
        .run {
            WidgetBottomBarElement(this)
        }

internal val WidgetRepository.refreshingBlocking: WidgetRefreshing
    get() = refreshingParametersEnablementMap
        .getSynchronousMap()
        .run {
            WidgetRefreshing(this)
        }