package com.w2sv.datastore.proto.widget_coloring

import androidx.datastore.core.DataStore
import com.w2sv.datastore.WidgetColoringProto
import com.w2sv.domain.model.WidgetColoring
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WidgetColoringDataSource @Inject constructor(private val widgetColoringProtoDataStore: DataStore<WidgetColoringProto>) {

    val config = widgetColoringProtoDataStore.data
        .map {
            WidgetColoringConfigMapper.toExternal(it)
        }
        .flowOn(Dispatchers.IO)

    suspend fun saveConfig(config: WidgetColoring.Config) {
        widgetColoringProtoDataStore.updateData {
            WidgetColoringConfigMapper.toProto(config)
        }
    }
}