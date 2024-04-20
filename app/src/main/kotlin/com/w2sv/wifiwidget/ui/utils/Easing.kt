package com.w2sv.wifiwidget.ui.utils

import android.view.animation.OvershootInterpolator
import com.w2sv.composed.extensions.toEasing

object Easing {
    val overshoot = OvershootInterpolator().toEasing()
}