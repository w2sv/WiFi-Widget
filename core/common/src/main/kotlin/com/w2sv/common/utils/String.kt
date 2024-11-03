package com.w2sv.common.utils

fun String.removeAlphanumeric(): String =
    replace(Regex("\\w"), "")
