package com.w2sv.common.extensions

import kotlin.math.roundToInt

fun Float.toRGBInt(): Int = (this * 255).roundToInt()
