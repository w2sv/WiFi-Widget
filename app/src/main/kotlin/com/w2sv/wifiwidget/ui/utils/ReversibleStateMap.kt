package com.w2sv.wifiwidget.ui.utils

import com.w2sv.composed.extensions.toMutableStateMap
import com.w2sv.datastoreutils.preferences.map.DataStoreFlowMap
import com.w2sv.reversiblestate.ReversibleStateMap
import com.w2sv.reversiblestate.datastore.android.reversibleStateMap
import kotlinx.coroutines.CoroutineScope

fun <K, V> DataStoreFlowMap<K, V>.reversibleStateMap(
    scope: CoroutineScope,
    onStateSynced: suspend (Map<K, V>) -> Unit = {}
): ReversibleStateMap<K, V> =
    reversibleStateMap(
        scope = scope,
        makeMap = { it.toMutableStateMap() },
        onStateSynced = onStateSynced,
        appliedStateMapBasedStateAlignment = true
    )
