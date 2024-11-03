package com.w2sv.common.utils

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class SuspendingLazy<out T>(initializer: suspend () -> T) {
    private var initializer: (suspend () -> T)? = initializer
    private var mutex: Mutex? = Mutex()
    private var value: Any? = UninitializedValue

    suspend fun value(): T {
        val v1 = value
        if (v1 !== UninitializedValue) {
            @Suppress("UNCHECKED_CAST")
            return v1 as T
        }
        return mutex!!.withLock {
            val v2 = value
            if (v2 !== UninitializedValue) {
                @Suppress("UNCHECKED_CAST")
                (v2 as T)
            } else {
                val typedValue = initializer!!.invoke()
                value = typedValue
                initializer = null
                mutex = null
                typedValue
            }
        }
    }

    val isInitialized: Boolean get() = value !== UninitializedValue

    override fun toString(): String =
        if (isInitialized) value.toString() else "SuspendingLazy value not initialized yet."
}

private object UninitializedValue
