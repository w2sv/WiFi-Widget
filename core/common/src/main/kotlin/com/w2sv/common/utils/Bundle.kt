package com.w2sv.common.utils

import android.os.Bundle

/**
 * For readable logging of the [Bundle] content.
 */
@Suppress("DEPRECATION")
fun Bundle.toMapString(): String { // TODO: move to AndroidUtils
    return keySet().joinToString(prefix = "{", postfix = "}") { key ->
        "$key=${
            get(key).run {
                when (this) {
                    is Bundle -> toMapString()
                    is Array<*> -> toList()
                    is IntArray -> toList()
                    is LongArray -> toList()
                    is FloatArray -> toList()
                    is DoubleArray -> toList()
                    is BooleanArray -> toList()
                    else -> this
                }
            }
        }"
    }
}
