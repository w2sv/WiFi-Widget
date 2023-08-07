package com.w2sv.wifiwidget.ui.utils

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.w2sv.androidutils.ui.UnconfirmedStateFlow
import com.w2sv.androidutils.ui.UnconfirmedStateMap
import com.w2sv.androidutils.ui.UnconfirmedStates
import com.w2sv.androidutils.ui.UnconfirmedStatesComposition
import kotlinx.coroutines.flow.Flow

// TODO: move to AndroidUtils

fun <K, V> ViewModel.getUnconfirmedStateMap(
    appliedFlowMap: Map<K, Flow<V>>,
    syncState: suspend (Map<K, V>) -> Unit
): UnconfirmedStateMap<K, V> =
    UnconfirmedStateMap(
        coroutineScope = viewModelScope,
        appliedFlowMap = appliedFlowMap,
        makeSynchronousMutableMap = { it.getSynchronousMutableStateMap() },
        syncState = syncState
    )

fun <T> ViewModel.getUnconfirmedStateFlow(
    appliedFlow: Flow<T>,
    syncState: suspend (T) -> Unit
): UnconfirmedStateFlow<T> =
    UnconfirmedStateFlow(
        coroutineScope = viewModelScope,
        appliedFlow = appliedFlow,
        syncState = syncState
    )

fun ViewModel.getUnconfirmedStatesComposition(
    unconfirmedStates: UnconfirmedStates,
    onStateSynced: suspend () -> Unit = {}
): UnconfirmedStatesComposition =
    UnconfirmedStatesComposition(
        unconfirmedStates = unconfirmedStates,
        coroutineScope = viewModelScope,
        onStateSynced = onStateSynced
    )