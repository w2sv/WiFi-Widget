package com.w2sv.wifiwidget.ui.utils

import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color

/**
 * Equals material3 __IconButtonTokens.DisabledIconOpacity__
 */
private const val DECREASED_ALPHA = 0.38f

fun Color.alphaDecreased(decreasedAlpha: Float = DECREASED_ALPHA): Color =
    copy(alpha = decreasedAlpha)

fun Color.orAlphaDecreasedIf(condition: Boolean, decreasedAlpha: Float = DECREASED_ALPHA): Color =
    if (condition) {
        copy(alpha = decreasedAlpha)
    } else {
        this
    }

@Composable
fun WithLocalContentColor(color: Color, content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalContentColor provides color, content)
}
