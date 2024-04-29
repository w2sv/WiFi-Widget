package com.w2sv.common.utils

import kotlin.time.Duration

val Duration.minutes: Int
    get() = (inWholeMinutes % 60).toInt()