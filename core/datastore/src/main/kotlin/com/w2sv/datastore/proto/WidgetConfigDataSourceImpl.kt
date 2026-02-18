package com.w2sv.datastore.proto

import androidx.datastore.core.DataStore
import com.w2sv.datastore.WifiWidgetConfigProto
import com.w2sv.datastore.proto.mapping.toExternal
import com.w2sv.datastore.proto.mapping.toProto
import com.w2sv.domain.model.widget.WifiWidgetConfig
import com.w2sv.domain.repository.WidgetConfigDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class WidgetConfigDataSourceImpl @Inject constructor(private val dataStore: DataStore<WifiWidgetConfigProto>) :
    WidgetConfigDataSource {

    override val config = dataStore.data
        .map { it.toExternal() }
        .flowOn(Dispatchers.IO)

    override suspend fun update(config: WifiWidgetConfig) {
        dataStore.updateData { config.toProto() }
    }
}
