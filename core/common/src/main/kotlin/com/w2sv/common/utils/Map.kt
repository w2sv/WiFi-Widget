package com.w2sv.common.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

val <T> Map<T, Boolean>.enabledKeys: List<T>
    get() = keys.filter { getValue(it) }

val <T> Map<T, StateFlow<Boolean>>.valueEnabledKeys: List<T>
    get() = keys.filter { getValue(it).value }

fun <K, V> Map<K, Flow<V>>.stateIn(
    scope: CoroutineScope,
    started: SharingStarted,
    initialValue: V
): Map<K, StateFlow<V>> =
    mapValues { (_, v) ->
        v.stateIn(
            scope,
            started,
            initialValue
        )
    }