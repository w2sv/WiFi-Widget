package com.w2sv.wifiwidget.ui.utils

import android.animation.TimeInterpolator
import androidx.compose.animation.core.Easing

fun TimeInterpolator.toEasing() = Easing {
    getInterpolation(it)
}