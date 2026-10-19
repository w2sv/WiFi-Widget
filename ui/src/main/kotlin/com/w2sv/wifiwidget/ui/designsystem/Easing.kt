package com.w2sv.wifiwidget.ui.designsystem

import android.view.animation.AnticipateInterpolator
import android.view.animation.Interpolator
import android.view.animation.OvershootInterpolator
import androidx.compose.animation.core.Easing
import com.w2sv.kotlinutils.threadUnsafeLazy

object Easing {
    val Overshoot by threadUnsafeLazy { OvershootInterpolator().asEasing() }
    val Anticipate by threadUnsafeLazy { AnticipateInterpolator().asEasing() }
}

private fun Interpolator.asEasing() =
    Easing(::getInterpolation)
