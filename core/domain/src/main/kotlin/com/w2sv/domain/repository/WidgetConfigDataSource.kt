package com.w2sv.domain.repository

import com.w2sv.domain.model.widget.WidgetConfig
import kotlinx.coroutines.flow.Flow

interface WidgetConfigDataSource {
    val config: Flow<WidgetConfig>
    suspend fun update(transform: (WidgetConfig) -> WidgetConfig)
}
