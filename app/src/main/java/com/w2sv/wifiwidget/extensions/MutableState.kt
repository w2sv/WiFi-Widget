package com.w2sv.wifiwidget.extensions

import androidx.compose.runtime.MutableState

fun MutableState<Boolean>.disable(){
    value = false
}
fun MutableState<Boolean>.enable(){
    value = true
}