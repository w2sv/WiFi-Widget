package com.w2sv.wifiwidget.ui.utils

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer

@Stable
class ShakeController(private val config: ShakeConfig = shakeConfig) {

    suspend fun shake() {
        for (i in 0..config.iterations) {
            animatable.animateTo(
                targetValue = if (i % 2 == 0) config.translateX else -config.translateX,
                animationSpec = spring(
                    stiffness = config.stiffness
                )
            )
        }
        animatable.animateTo(0f)
    }

    private val animatable = Animatable(0f)

    internal val offset: Float
        get() = animatable.value
}

private val shakeConfig = ShakeConfig(
    iterations = 2,
    translateX = 20f,
    stiffness = Spring.StiffnessHigh
)

@Immutable
data class ShakeConfig(val iterations: Int, val translateX: Float, val stiffness: Float = Spring.StiffnessMedium)

fun Modifier.shake(controller: ShakeController): Modifier =
    graphicsLayer { translationX = controller.offset }
