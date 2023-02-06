package com.w2sv.common

enum class Theme {
    Light,
    SystemDefault,
    Dark;

    companion object {
        operator fun get(ordinal: Int): Theme =
            values()[ordinal]
    }
}