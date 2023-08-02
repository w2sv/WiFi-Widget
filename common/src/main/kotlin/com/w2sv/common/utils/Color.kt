package com.w2sv.common.utils

import androidx.annotation.ColorInt
import androidx.annotation.FloatRange
import androidx.core.graphics.ColorUtils
import kotlin.math.roundToInt

@ColorInt
fun @receiver:FloatRange(0.0, 1.0) Float.toRGBChannelInt(): Int = (this * 255).roundToInt()

@ColorInt
fun getAlphaSetColor(@ColorInt color: Int, @FloatRange(0.0, 1.0) opacity: Float): Int =
    ColorUtils.setAlphaComponent(
        color,
        opacity.toRGBChannelInt()
    )
