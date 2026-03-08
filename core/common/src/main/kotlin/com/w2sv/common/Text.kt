package com.w2sv.common

import android.content.res.Resources
import androidx.annotation.StringRes

sealed interface Text {

    fun resolve(resources: Resources): String =
        when (this) {
            is Raw -> value
            is Resource -> resources.getString(value)
        }

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
