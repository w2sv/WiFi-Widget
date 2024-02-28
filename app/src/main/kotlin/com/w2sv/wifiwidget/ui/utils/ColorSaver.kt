package com.w2sv.wifiwidget.ui.utils

import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb

val colorSaver = Saver<Color, Int>(save = { it.toArgb() }, restore = { Color(it) })
val nullableColorSaver = listSaver<Color?, Float>(
    save = { color ->
        buildList {
            color?.let {
                add(it.red)
                add(it.green)
                add(it.blue)
                add(it.alpha)
            }
        }
    },
    restore = {
        if (it.isEmpty()) {
            null
        } else {
            Color(red = it[0], green = it[1], blue = it[2], alpha = it[3])
        }
    }
)
