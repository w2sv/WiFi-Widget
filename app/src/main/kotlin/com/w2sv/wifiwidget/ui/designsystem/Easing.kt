package com.w2sv.wifiwidget.ui.designsystem

import android.view.animation.AnticipateInterpolator
import android.view.animation.OvershootInterpolator
import com.w2sv.composed.core.extensions.toEasing
import com.w2sv.kotlinutils.threadUnsafeLazy

object Easing {
    val overshoot by threadUnsafeLazy { OvershootInterpolator().toEasing() }
    val anticipate by threadUnsafeLazy { AnticipateInterpolator().toEasing() }
}
