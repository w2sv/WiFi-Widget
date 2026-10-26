package com.w2sv.common.logging

import slimber.log.i

inline fun <T> T.log(makeMessage: (T) -> String = { it.toString() }): T =
    also { i { makeMessage(this) } }

@Suppress("NOTHING_TO_INLINE")
inline fun <T> T.log(prefix: String): T =
    also { i { "$prefix=$this" } }
