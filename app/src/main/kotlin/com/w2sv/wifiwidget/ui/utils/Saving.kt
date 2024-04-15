package com.w2sv.wifiwidget.ui.utils

import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.SaverScope
import androidx.compose.runtime.saveable.listSaver

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
