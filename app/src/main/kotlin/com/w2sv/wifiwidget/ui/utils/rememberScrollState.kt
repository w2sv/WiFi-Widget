package com.w2sv.wifiwidget.ui.utils

import androidx.compose.foundation.ScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.rememberSaveable

@Composable
fun rememberScrollState(initial: Int = 0, key: String? = null): ScrollState {
    return rememberSaveable(saver = ScrollState.Saver, key = key) {
        ScrollState(initial = initial)
    }
}