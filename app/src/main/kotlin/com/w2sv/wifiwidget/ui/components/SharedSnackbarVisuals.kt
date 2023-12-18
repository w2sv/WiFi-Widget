package com.w2sv.wifiwidget.ui.components

import androidx.compose.material3.SnackbarVisuals
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SharedSnackbarVisuals @Inject constructor() {
    val flow get() = _flow.asSharedFlow()
    private val _flow = MutableSharedFlow<SnackbarVisuals>()

    suspend fun emit(visuals: SnackbarVisuals) {
        _flow.emit(visuals)
    }
}