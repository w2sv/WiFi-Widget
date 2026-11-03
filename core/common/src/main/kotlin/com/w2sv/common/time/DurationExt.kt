package com.w2sv.common.time

import kotlin.time.Duration

val Duration.minutes: Int
    get() = (inWholeMinutes % 60).toInt()
