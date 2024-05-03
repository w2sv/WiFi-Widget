package com.w2sv.wifiwidget.ui.designsystem

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.ui.unit.IntOffset
import androidx.navigation.NavBackStackEntry
import com.ramcosta.composedestinations.spec.DestinationStyle
import com.w2sv.wifiwidget.ui.utils.Easing

private typealias NavigationEnterTransition = (AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition?)
private typealias NavigationExitTransition = (AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition?)

abstract class PopNonPopIdenticalAnimatedDestinationStyle : DestinationStyle.Animated() {
    abstract override val enterTransition: NavigationEnterTransition
    abstract override val exitTransition: NavigationExitTransition

    override val popEnterTransition: NavigationEnterTransition get() = enterTransition
    override val popExitTransition: NavigationExitTransition get() = exitTransition
}

object HorizontalSlideTransitions : PopNonPopIdenticalAnimatedDestinationStyle() {
    override val enterTransition: NavigationEnterTransition = {
        slideInHorizontally(animationSpec = animationSpec) + fadeIn(
            tween(
                durationMillis
            )
        )
    }
    override val exitTransition: NavigationExitTransition = {
        slideOutHorizontally(animationSpec = animationSpec) + fadeOut(
            tween(
                durationMillis
            )
        )
    }

    private const val durationMillis: Int = 500

    private val animationSpec =
        tween<IntOffset>(durationMillis = durationMillis, easing = Easing.overshoot)
}