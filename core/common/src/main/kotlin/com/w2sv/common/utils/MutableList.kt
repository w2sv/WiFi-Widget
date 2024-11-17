package com.w2sv.common.utils

import androidx.annotation.IntRange

fun <T> MutableList<T>.moveElement(@IntRange(from = 0) fromIndex: Int, @IntRange(from = 0) toIndex: Int) {  // TODO: kotlinutils
    if (fromIndex == toIndex) return
    add(toIndex, removeAt(fromIndex))
}
