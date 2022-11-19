package com.w2sv.wifiwidget.utils.extensions

import androidx.compose.runtime.MutableState

fun MutableState<Boolean>.disable() {
    value = false
}

fun MutableState<Boolean>.enable() {
    value = true
}

fun MutableState<Boolean>.toggle() {
    value = !value
}