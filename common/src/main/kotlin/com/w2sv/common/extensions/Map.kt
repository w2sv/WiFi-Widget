package com.w2sv.common.extensions

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

fun <K, V> Map<K, Flow<V>>.getDeflowedMap(): Map<K, V> =
    runBlocking {
        mapValues {
            it.value.first()
        }
    }