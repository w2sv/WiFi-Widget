package com.w2sv.domain.wifiproperty

import androidx.annotation.StringRes

sealed interface Text {

    @JvmInline
    value class Raw(val value: String) : Text

    @JvmInline
    value class Resource(@StringRes val value: Int) : Text

    companion object {
        operator fun invoke(value: String) =
            Raw(value)
        operator fun invoke(@StringRes value: Int) =
            Resource(value)
    }
}

val Int.txt: Text.Resource get() = Text(this)
val String.txt: Text.Raw get() = Text(this)
