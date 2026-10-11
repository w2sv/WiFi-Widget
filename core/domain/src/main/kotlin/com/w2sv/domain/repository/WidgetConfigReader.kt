package com.w2sv.domain.repository

import com.w2sv.domain.model.widget.WidgetConfig

/**
 * Centralizes synchronous and asynchronous reading of the current [WidgetConfig].
 */
interface WidgetConfigReader {
    suspend fun read(): WidgetConfig
    fun readBlocking(): WidgetConfig
}
