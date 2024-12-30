package com.w2sv.widget.data

import com.w2sv.domain.repository.WidgetRepository
import com.w2sv.widget.model.WidgetAppearance
import com.w2sv.widget.model.WidgetBottomBarElement
import com.w2sv.widget.model.WidgetRefreshing
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

internal val WidgetRepository.appearanceBlocking: WidgetAppearance
    get() = runBlocking { appearance() }

internal suspend fun WidgetRepository.appearance(): WidgetAppearance =
    WidgetAppearance(
        coloringConfig = coloringConfig.first(),
        backgroundOpacity = opacity.first(),
        fontSize = fontSize.first(),
        propertyValueAlignment = propertyValueAlignment.first(),
        bottomBar = bottomRowElementEnablementMap
            .mapValuesToFirst()
            .run { WidgetBottomBarElement(this) }
    )

internal val WidgetRepository.refreshingBlocking: WidgetRefreshing
    get() = runBlocking {
        refreshingParametersEnablementMap
            .mapValuesToFirst()
            .run { WidgetRefreshing(parameters = this, interval = refreshInterval.first()) }
    }

private suspend fun <K, V> Map<K, Flow<V>>.mapValuesToFirst(): Map<K, V> =  // TODO: move to KotlinUtils
    mapValues { (_, v) -> v.first() }
