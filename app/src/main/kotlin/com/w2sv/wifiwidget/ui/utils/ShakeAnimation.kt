package com.w2sv.wifiwidget.ui.utils

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

@Composable
fun rememberShakeController(key1: Any? = null): ShakeController {
    return remember(key1) { ShakeController() }
}

class ShakeController {
    fun shake(shakeConfig: ShakeConfig) {
        this.config = shakeConfig
    }

    var config: ShakeConfig? by mutableStateOf(null)
        private set

    internal fun resetConfig() {
        config = null
    }
}

@Immutable
data class ShakeConfig(
    val iterations: Int,
    val translateX: Float = 0f,
    val stiffness: Float = Spring.StiffnessMedium,
)

fun Modifier.shake(controller: ShakeController) = composed {
    controller.config?.let { config ->
        val shake = remember(config) { Animatable(0f) }
        LaunchedEffect(config) {
            for (i in 0..config.iterations) {
                shake.animateTo(
                    targetValue = if (i % 2 == 0) 1f else -1f,
                    animationSpec = spring(
                        stiffness = config.stiffness,
                    )
                )
            }
            shake.animateTo(0f)
            controller.resetConfig()
        }

        offset(x = (shake.value * config.translateX).roundToInt().dp)
    } ?: this
}