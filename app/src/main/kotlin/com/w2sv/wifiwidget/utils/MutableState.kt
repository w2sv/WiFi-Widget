package com.w2sv.wifiwidget.utils

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.snapshots.SnapshotStateMap

fun MutableState<Boolean>.disable() {
    value = false
}

fun MutableState<Boolean>.enable() {
    value = true
}

fun MutableState<Boolean>.toggle() {
    value = !value
}

fun <K, V> Map<K, V>.getMutableStateMap(): SnapshotStateMap<K, V> =
    mutableStateMapOf<K, V>()
        .apply { putAll(this@getMutableStateMap) }