package com.w2sv.wifiwidget.ui.utils

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
