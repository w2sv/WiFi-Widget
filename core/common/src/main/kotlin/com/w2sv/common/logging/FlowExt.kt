package com.w2sv.common.logging

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import slimber.log.i

@Suppress("NOTHING_TO_INLINE")
inline fun <T> Flow<T>.logOnEach(tag: String): Flow<T> =
    onEach { value -> i { "$tag emitted $value" } }

@Suppress("NOTHING_TO_INLINE")
inline fun <T> Flow<T>.logOnCancellation(tag: String): Flow<T> =
    onCompletion { cause -> cause?.log { "$tag cancelled with $it" } }
