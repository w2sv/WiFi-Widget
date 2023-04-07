package com.w2sv.common.extensions

import kotlin.math.roundToInt

fun Float.toRGBChannelInt(): Int = (this * 255).roundToInt()
