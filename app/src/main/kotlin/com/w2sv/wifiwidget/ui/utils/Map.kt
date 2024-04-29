package com.w2sv.wifiwidget.ui.utils

import com.w2sv.androidutils.datastorage.preferences_datastore.flow.DataStoreFlowMap
import com.w2sv.androidutils.ui.reversible_state.ReversibleStateMap
import com.w2sv.composed.extensions.toMutableStateMap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted

fun <K, V> ReversibleStateMap.Companion.fromDataStoreFlowMap(
    dataStoreFlowMap: DataStoreFlowMap<K, V>,
    scope: CoroutineScope,
    onStateSynced: suspend (Map<K, V>) -> Unit = {}
): ReversibleStateMap<K, V> {
    return ReversibleStateMap(
        appliedStateMap = dataStoreFlowMap.stateIn(scope, SharingStarted.WhileSubscribed()),
        makeMap = {
            it.toMutableStateMap()
        },
        syncState = dataStoreFlowMap::save,
        onStateSynced = onStateSynced,
        appliedStateMapBasedStateAlignmentAssuranceScope = scope
    )
}