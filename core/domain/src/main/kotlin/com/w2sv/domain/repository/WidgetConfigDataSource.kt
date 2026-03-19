package com.w2sv.domain.repository

import com.w2sv.domain.model.widget.WidgetConfig
import kotlinx.coroutines.flow.Flow

typealias WidgetConfigFlow = Flow<WidgetConfig>

interface WidgetConfigDataSource {
    val config: WidgetConfigFlow
    suspend fun update(transform: (WidgetConfig) -> WidgetConfig)
}
