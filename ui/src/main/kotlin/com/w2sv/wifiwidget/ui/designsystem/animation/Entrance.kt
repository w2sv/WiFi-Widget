/*
 * Adapted from Voice by Paul Woitaschek and contributors (GNU GPL v3):
 * https://github.com/PaulWoitaschek/Voice/blob/main/core/ui/src/main/kotlin/voice/core/ui/Entrance.kt
 * https://github.com/PaulWoitaschek/Voice/blob/main/core/ui/src/main/kotlin/voice/core/ui/OnboardingScaffold.kt
 * See the repository's LICENSE for the GNU General Public License v3.
 */
package com.w2sv.wifiwidget.ui.designsystem.animation

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOutBack
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp

/** Shared progress for a short sequence of elements entering without changing their layout. */
@Stable
class EntranceState internal constructor(initialProgress: Float) {
    internal val progress = Animatable(initialProgress)

    internal fun elementProgress(index: Int): Float =
        ((progress.value - index * EntranceTokens.staggerOffset) / EntranceTokens.animationWindow).coerceIn(0f, 1f)
}

/** Plays once, retaining whether entrance has started across restoration; [animate] controls initial progress only. */
@Composable
fun rememberEntranceState(animate: Boolean = true): EntranceState {
    var entered by rememberSaveable { mutableStateOf(false) }

    val state = remember {
        EntranceState(
            initialProgress = if (animate && !entered) 0f else 1f
        )
    }

    LaunchedEffect(state) {
        entered = true
        if (state.progress.value < 1f) {
            state.progress.animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = EntranceTokens.durationMillis,
                    easing = EntranceTokens.progressEasing
                )
            )
        }
    }

    return state
}

/** Higher indices enter later. A full staggered window must fit within shared progress from zero to one. */
fun Modifier.entrance(state: EntranceState, index: Int): Modifier =
    graphicsLayer {
        val local = state.elementProgress(index)
        alpha = local
        translationY = (1f - EntranceTokens.translationEasing.transform(local)) * EntranceTokens.initialTranslation.toPx()
    }

private object EntranceTokens {
    val durationMillis = 900
    val staggerOffset = 0.08f
    val animationWindow = 0.5f
    val progressEasing = LinearEasing
    val translationEasing = EaseOutBack
    val initialTranslation = 26.dp
}
