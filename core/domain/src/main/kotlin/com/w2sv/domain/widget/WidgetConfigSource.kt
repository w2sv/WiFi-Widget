package com.w2sv.domain.widget

import kotlinx.coroutines.flow.Flow

/**
 * Centralizes read-only access to [WidgetConfig].
 */
interface WidgetConfigSource {
    val flow: Flow<WidgetConfig>
    suspend fun snapshot(): WidgetConfig
    fun blockingSnapshot(): WidgetConfig
}
