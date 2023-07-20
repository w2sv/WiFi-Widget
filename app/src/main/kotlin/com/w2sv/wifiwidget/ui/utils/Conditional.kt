package com.w2sv.wifiwidget.ui.utils

import androidx.compose.ui.Modifier

fun Modifier.conditional(condition: Boolean, modifier: Modifier.() -> Modifier): Modifier {
    return if (condition) {
        then(modifier(Modifier))
    } else {
        this
    }
}