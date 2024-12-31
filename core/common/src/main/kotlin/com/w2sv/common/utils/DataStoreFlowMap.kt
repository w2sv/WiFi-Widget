package com.w2sv.common.utils

import com.w2sv.datastoreutils.preferences.map.DataStoreFlowMap
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

fun <K> DataStoreFlowMap<K, Boolean>.enabledKeysFlow(): Flow<Set<K>> =
    combine(
        entries.map { (k, v) ->
            v.map { k to it }
        }
    ) {
        buildSet {
            it.forEach { (k, isEnabled) ->
                if (isEnabled) {
                    add(k)
                }
            }
        }
    }

fun <K, V> DataStoreFlowMap<K, V>.mapFlow(): Flow<Map<K, V>> =
    combine(
        entries.map { (k, v) ->
            v.map { k to it }
        }
    ) { it.toMap() }
