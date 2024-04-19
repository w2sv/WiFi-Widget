package com.w2sv.wifiwidget.ui.utils

import androidx.compose.runtime.mutableStateMapOf
import com.w2sv.androidutils.datastorage.preferences_datastore.flow.DataStoreFlowMap
import com.w2sv.androidutils.ui.reversible_state.ReversibleStateMap
import kotlinx.coroutines.CoroutineScope

fun <K, V> ReversibleStateMap.Companion.fromDataStoreFlowMap(
    dataStoreFlowMap: DataStoreFlowMap<K, V>,
    scope: CoroutineScope,
    onStateSynced: suspend (Map<K, V>) -> Unit = {}
): ReversibleStateMap<K, V> =
    fromPersistedFlowMapWithSynchronousInitial(
        persistedFlowMap = dataStoreFlowMap,
        scope = scope,
        makeMap = {
            mutableStateMapOf<K, V>().apply {
                putAll(it)
            }
        },
        syncState = dataStoreFlowMap::save,
        onStateSynced = onStateSynced
    )