package com.w2sv.wifiwidget.ui.utils

import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.SaverScope
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb

val colorSaver = Saver<Color, Int>(save = { it.toArgb() }, restore = { Color(it) })
val nullableColorSaver = nullableListSaver<Color, Float>(
    saveNonNull = {
        listOf(it.red, it.green, it.blue, it.alpha)
    },
    restoreNonNull = {
        Color(red = it[0], green = it[1], blue = it[2], alpha = it[3])
    }
)

fun <Original, Saveable> nullableListSaver(
    saveNonNull: SaverScope.(value: Original) -> List<Saveable>,
    restoreNonNull: (list: List<Saveable>) -> Original?
): Saver<Original?, Any> =
    listSaver(
        save = { original ->
            original?.let { saveNonNull(it) } ?: emptyList()
        },
        restore = {
            if (it.isEmpty()) {
                null
            } else {
                restoreNonNull(it)
            }
        }
    )
