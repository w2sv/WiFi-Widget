package com.w2sv.datastore.proto

import androidx.datastore.core.DataStore
import com.w2sv.datastore.WidgetConfigProto
import com.w2sv.datastore.proto.mapping.toExternal
import com.w2sv.datastore.proto.mapping.toProto
import com.w2sv.domain.model.widget.WidgetConfig
import com.w2sv.domain.repository.WidgetConfigDataSource
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.map

@Singleton
internal class WidgetConfigDataSourceImpl @Inject constructor(private val dataStore: DataStore<WidgetConfigProto>) :
    WidgetConfigDataSource {

    override val config = dataStore.data.map { it.toExternal() }

    override suspend fun update(transform: (WidgetConfig) -> WidgetConfig) {
        dataStore.updateData { transform(it.toExternal()).toProto() }
    }
}
