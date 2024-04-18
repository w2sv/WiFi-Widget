package com.w2sv.wifiwidget.ui.utils

import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.snapshots.SnapshotStateMap
import com.w2sv.androidutils.datastorage.preferences_datastore.flow.DataStoreFlowMap
import com.w2sv.androidutils.ui.reversible_state.ReversibleStateMap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow

fun <K, V> ReversibleStateMap.Companion.fromDataStoreFlowMap(
    dataStoreFlowMap: DataStoreFlowMap<K, V>,
    scope: CoroutineScope,
    started: SharingStarted = SharingStarted.Eagerly,
    onStateSynced: suspend (Map<K, V>) -> Unit = {}
): ReversibleStateMap<K, V> =
    ReversibleStateMap(
        persistedStateFlowMap = dataStoreFlowMap.stateIn(scope, started),
        makeMap = { it.getMutableStateMap() },
        syncState = dataStoreFlowMap::save,
        onStateSynced = onStateSynced
    )

private fun <K, V> Map<K, StateFlow<V>>.getMutableStateMap(): SnapshotStateMap<K, V> =
    mutableStateMapOf<K, V>()
        .apply { this@getMutableStateMap.forEach { (k, v) -> put(k, v.value) } }