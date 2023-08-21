package com.w2sv.wifiwidget.ui.utils

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.w2sv.androidutils.ui.unconfirmed_state.UnconfirmedStateMap
import kotlinx.coroutines.flow.Flow

fun <K, V> ViewModel.getUnconfirmedStateMap(
    appliedFlowMap: Map<K, Flow<V>>,
    syncState: suspend (Map<K, V>) -> Unit,
): UnconfirmedStateMap<K, V> =
    UnconfirmedStateMap(
        coroutineScope = viewModelScope,
        appliedFlowMap = appliedFlowMap,
        makeSynchronousMutableMap = { it.getSynchronousMutableStateMap() },
        syncState = syncState,
    )
