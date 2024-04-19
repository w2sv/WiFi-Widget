package com.w2sv.wifiwidget.ui.utils

import androidx.compose.runtime.mutableStateMapOf
import com.w2sv.androidutils.coroutines.collectFromFlow
import com.w2sv.androidutils.datastorage.preferences_datastore.flow.DataStoreFlowMap
import com.w2sv.androidutils.ui.reversible_state.ReversibleStateMap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take

fun <K, V> ReversibleStateMap.Companion.fromDataStoreFlowMap(
    dataStoreFlowMap: DataStoreFlowMap<K, V>,
    scope: CoroutineScope,
    onStateSynced: suspend (Map<K, V>) -> Unit = {}
): ReversibleStateMap<K, V> {
    val persistedStateFlowMap = dataStoreFlowMap.stateIn(scope, SharingStarted.WhileSubscribed())
    val map = mutableStateMapOf<K, V>().apply {
        putAll(persistedStateFlowMap.mapValues { (_, v) -> v.value })
    }
    persistedStateFlowMap.forEach { (k, v) ->
        scope.collectFromFlow(v.take(2)) {
            map[k] = v.first()
        }
    }
    return ReversibleStateMap(
        persistedStateFlowMap = persistedStateFlowMap,
        map = map,
        syncState = dataStoreFlowMap::save,
        onStateSynced = onStateSynced
    )
}