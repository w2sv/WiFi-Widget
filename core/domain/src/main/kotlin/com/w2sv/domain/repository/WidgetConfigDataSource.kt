package com.w2sv.domain.repository

import com.w2sv.domain.model.widget.WifiWidgetConfig
import kotlinx.coroutines.flow.Flow

typealias WidgetConfigFlow = Flow<WifiWidgetConfig>

interface WidgetConfigDataSource {
    val config: WidgetConfigFlow
    suspend fun update(config: WifiWidgetConfig)
}
