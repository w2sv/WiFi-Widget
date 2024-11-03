package com.w2sv.wifiwidget.ui.designsystem

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.navigation.NavBackStackEntry
import com.ramcosta.composedestinations.spec.DestinationStyle

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
        slideInHorizontally() + fadeIn()
    }
    override val exitTransition: NavigationExitTransition = {
        slideOutHorizontally() + fadeOut()
    }
}
