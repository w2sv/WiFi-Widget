package com.w2sv.wifiwidget.ui.utils

import android.annotation.SuppressLint
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.unit.dp

@Stable
class ShakeController(private val config: ShakeConfig) {
    fun shake() {
        doShake = true
    }

    internal suspend fun animate(animatable: Animatable<Float, AnimationVector1D>) {
        for (i in 0..config.iterations) {
            animatable.animateTo(
                targetValue = if (i % 2 == 0) config.translateX else -config.translateX,
                animationSpec = spring(
                    stiffness = config.stiffness,
                )
            )
        }
        animatable.animateTo(0f)
        reset()
    }

    internal var doShake by mutableStateOf(false)

    private fun reset() {
        doShake = false
    }
}

@Immutable
data class ShakeConfig(
    val iterations: Int,
    val translateX: Float,
    val stiffness: Float = Spring.StiffnessMedium,
)

@SuppressLint("ComposeModifierComposed")
fun Modifier.shake(controller: ShakeController) = composed {
    val shake = remember { Animatable(0f) }

    LaunchedEffect(controller.doShake) {
        if (controller.doShake) {
            controller.animate(shake)
        }
    }

    offset(x = shake.value.dp)
}