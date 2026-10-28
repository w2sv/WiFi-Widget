package com.w2sv.wifiwidget.ui.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.PathEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp

private val emphasizedEasing = PathEasing(
    Path().apply {
        moveTo(0f, 0f)
        cubicTo(0.05f, 0f, 0.133333f, 0.06f, 0.166666f, 0.4f)
        cubicTo(0.208333f, 0.82f, 0.25f, 1f, 1f, 1f)
    }
)

private val emphasizedAccelerateEasing =
    CubicBezierEasing(0.3f, 0f, 0.8f, 0.15f)

/** Fade-and-slide entrance for forward navigation. */
internal val xAxisEnterTransition: EnterTransition =
    fadeIn(
        animationSpec = tween(
            durationMillis = 450,
            easing = emphasizedEasing
        )
    ) + slideInHorizontally(
        animationSpec = tween(
            durationMillis = 500,
            easing = emphasizedEasing
        )
    ) { width ->
        width / 2
    }

/** Fade-and-slide exit for forward navigation. */
internal fun xAxisExitTransition(density: Density): ExitTransition =
    fadeOut(
        animationSpec = tween(
            durationMillis = 250,
            easing = emphasizedAccelerateEasing
        )
    ) + slideOutHorizontally(
        animationSpec = tween(
            durationMillis = 300,
            easing = emphasizedAccelerateEasing
        )
    ) {
        with(density) { -30.dp.roundToPx() }
    }

/** Fade-and-scale entrance for regular and predictive Back. */
internal val zAxisEnterTransition: EnterTransition =
    fadeIn(
        animationSpec = tween(
            durationMillis = 450,
            easing = emphasizedEasing
        )
    ) + scaleIn(
        initialScale = 0.8f,
        transformOrigin = TransformOrigin(0.5f, 1f),
        animationSpec = tween(
            durationMillis = 500,
            easing = emphasizedEasing
        )
    )

/** Fade-and-scale exit for regular and predictive Back. */
internal val zAxisExitTransition: ExitTransition =
    fadeOut(
        animationSpec = tween(
            durationMillis = 250,
            easing = emphasizedAccelerateEasing
        )
    ) + scaleOut(
        targetScale = 0.8f,
        transformOrigin = TransformOrigin(0.5f, 1f),
        animationSpec = tween(
            durationMillis = 300,
            easing = emphasizedAccelerateEasing
        )
    )
