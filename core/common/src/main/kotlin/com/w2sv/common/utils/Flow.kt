package com.w2sv.common.utils

import com.hoc081098.flowext.withLatestFrom
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import slimber.log.i

@Suppress("NOTHING_TO_INLINE")
inline fun <T> Flow<T>.logOnEach(tag: String): Flow<T> =
    onEach { value -> i { "$tag emitted $value" } }

@Suppress("NOTHING_TO_INLINE")
inline fun <T> Flow<T>.logOnCancellation(tag: String): Flow<T> =
    onCompletion { cause -> cause?.log { "$tag cancelled with $it" } }

/**
 * Emits all values from this flow and re-emits the latest value
 * whenever [trigger] emits. Useful for manual refresh or retry.
 */
fun <T> Flow<T>.refreshOn(trigger: Flow<*>): Flow<T> =
    merge(
        this,
        trigger.withLatestFrom(this) { _, value -> value }
    )
