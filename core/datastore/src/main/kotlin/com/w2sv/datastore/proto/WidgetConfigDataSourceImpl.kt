package com.w2sv.datastore.proto

import androidx.datastore.core.DataStore
import com.w2sv.common.di.AppDefaultScope
import com.w2sv.datastore.WidgetConfigProto
import com.w2sv.datastore.proto.mapping.toExternal
import com.w2sv.datastore.proto.mapping.toProto
import com.w2sv.domain.model.widget.WidgetConfig
import com.w2sv.domain.repository.WidgetConfigDataSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import slimber.log.i
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class WidgetConfigDataSourceImpl @Inject constructor(
    private val dataStore: DataStore<WidgetConfigProto>,
    @AppDefaultScope private val scope: CoroutineScope
) :
    WidgetConfigDataSource {

    override val config = dataStore.data
        .map { it.toExternal() }
        .shareIn(scope, SharingStarted.WhileSubscribed(), replay = 1) // Share to avoid redundant .toExternal calls

    override suspend fun update(transform: (WidgetConfig) -> WidgetConfig) {
        dataStore.updateData { transform(it.toExternal()).toProto() }
    }
}
