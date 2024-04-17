package com.w2sv.datastore

import androidx.datastore.core.DataStore
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
            it.toExternal()
        }
        .flowOn(Dispatchers.IO)

    suspend fun saveConfig(config: WidgetColoring.Config) {
        widgetColoringProtoDataStore.updateData {
            config.toProto()
        }
    }
}