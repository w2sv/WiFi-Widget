package com.w2sv.wifiwidget.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.core.EaseOutElastic
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun SpringAnimatedVisibility(
    visible: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable AnimatedVisibilityScope.() -> Unit
) {
    AnimatedVisibility(
        modifier = modifier,
        visible = visible,
        enter = fadeIn(animationSpec = tween(1000)) +
                expandVertically(
                    animationSpec = tween(
                        1000,
                        easing = EaseOutElastic
                    )
                ),
        exit = fadeOut(animationSpec = tween(1000)) +
                shrinkVertically(
                    animationSpec = tween(
                        1000,
                        easing = EaseOutElastic
                    )
                ),
        content = content
    )
}