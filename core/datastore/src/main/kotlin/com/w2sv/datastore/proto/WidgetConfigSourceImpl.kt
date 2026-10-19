package com.w2sv.datastore.proto

import com.w2sv.domain.widget.WidgetConfig
import com.w2sv.domain.widget.WidgetConfigDataSource
import com.w2sv.domain.widget.WidgetConfigSource
import javax.inject.Inject
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

internal class WidgetConfigSourceImpl @Inject constructor(dataSource: WidgetConfigDataSource) : WidgetConfigSource {

    override val flow = dataSource.config
    override suspend fun snapshot(): WidgetConfig =
        flow.first()

    override fun blockingSnapshot(): WidgetConfig =
        runBlocking { snapshot() }
}
