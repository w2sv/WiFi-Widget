package com.w2sv.wifiwidget.ui.utils

import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.snapshots.SnapshotStateMap
import com.w2sv.androidutils.ui.unconfirmed_state.UnconfirmedStateMap
import kotlinx.coroutines.flow.StateFlow

fun <K, V> UnconfirmedStateMap.Companion.fromStateFlowMap(
    stateFlowMap: Map<K, StateFlow<V>>,
    syncState: suspend (Map<K, V>) -> Unit,
    onStateSynced: suspend (Map<K, V>) -> Unit = {}
): UnconfirmedStateMap<K, V> =
    UnconfirmedStateMap(
        persistedStateFlowMap = stateFlowMap,
        makeMap = { it.getMutableStateMap() },
        syncState = syncState,
        onStateSynced = onStateSynced
    )

private fun <K, V> Map<K, StateFlow<V>>.getMutableStateMap(): SnapshotStateMap<K, V> =
    mutableStateMapOf<K, V>()
        .apply { this@getMutableStateMap.forEach { (k, v) -> put(k, v.value) } }