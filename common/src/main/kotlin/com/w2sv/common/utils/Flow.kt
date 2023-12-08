package com.w2sv.common.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

// TODO: export to AndroidUtils
suspend fun MutableSharedFlow<Unit>.trigger() {
    emit(Unit)
}

// TODO: export to AndroidUtils
fun <T> CoroutineScope.collectLatestFromFlow(
    flow: Flow<T>,
    context: CoroutineContext = EmptyCoroutineContext,
    start: CoroutineStart = CoroutineStart.DEFAULT,
    action: suspend (value: T) -> Unit
): Job =
    launch(context, start) {
        flow.collectLatest(action)
    }