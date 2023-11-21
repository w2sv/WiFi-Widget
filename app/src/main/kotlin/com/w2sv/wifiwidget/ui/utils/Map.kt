package com.w2sv.wifiwidget.ui.utils

import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.snapshots.SnapshotStateMap
import com.w2sv.androidutils.ui.unconfirmed_state.UnconfirmedStateMap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow

fun <K, V> Map<K, V>.getMutableStateMap(): SnapshotStateMap<K, V> =
    mutableStateMapOf<K, V>()
        .apply { putAll(this@getMutableStateMap) }

fun <K, V> UnconfirmedStateMap.Companion.fromPersistedFlowMapWithSynchronousInitialAsMutableStateMap(
    persistedFlowMap: Map<K, Flow<V>>,
    scope: CoroutineScope,
    syncState: suspend (Map<K, V>) -> Unit,
    onStateSynced: suspend (Map<K, V>) -> Unit = {}
): UnconfirmedStateMap<K, V> = fromPersistedFlowMapWithSynchronousInitial(
    persistedFlowMap = persistedFlowMap,
    scope = scope,
    makeMap = { it.getMutableStateMap() },
    syncState = syncState,
    onStateSynced = onStateSynced
)
