package com.w2sv.common.utils

import kotlinx.coroutines.flow.MutableSharedFlow

suspend fun MutableSharedFlow<Unit>.trigger() {
    emit(Unit)
}