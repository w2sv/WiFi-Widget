package com.w2sv.common.utils

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class SuspendingLazy<out T>(initializer: suspend () -> T) {
    private var initializer: (suspend () -> T)? = initializer
    private var mutex: Mutex? = Mutex()
    private var _value: Any? = UninitializedValue

    suspend fun value(): T {
        val _v1 = _value
        if (_v1 !== UninitializedValue) {
            @Suppress("UNCHECKED_CAST")
            return _v1 as T
        }
        return mutex!!.withLock {
            val _v2 = _value
            if (_v2 !== UninitializedValue) {
                @Suppress("UNCHECKED_CAST") (_v2 as T)
            } else {
                val typedValue = initializer!!.invoke()
                _value = typedValue
                initializer = null
                mutex = null
                typedValue
            }
        }
    }

    val isInitialized: Boolean get() = _value !== UninitializedValue

    override fun toString(): String =
        if (isInitialized) _value.toString() else "SuspendingLazy value not initialized yet."
}

private object UninitializedValue