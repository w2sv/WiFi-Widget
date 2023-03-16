package com.w2sv.wifiwidget.utils

import kotlinx.coroutines.flow.MutableStateFlow

fun <T> MutableStateFlow<T?>.reset() {
    value = null
}

fun MutableStateFlow<Boolean>.resetBoolean() {
    value = false
}