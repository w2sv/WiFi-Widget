package com.w2sv.widget.actions

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

@Singleton
internal class WidgetPinSuccess @Inject constructor() {

    val flow: SharedFlow<Unit>
        field = MutableSharedFlow()

    suspend fun emit() {
        flow.emit(Unit)
    }
}
