package com.w2sv.domain.widget

import kotlinx.coroutines.flow.Flow

interface WidgetConfigDataSource {
    val config: Flow<WidgetConfig>
    suspend fun update(transform: (WidgetConfig) -> WidgetConfig)
}
