package com.w2sv.widget.ui.theme

import com.w2sv.domain.widget.FontSize

internal val FontSize.textSize: Float
    get() = when (this) {
        FontSize.VerySmall -> 12f
        FontSize.Small -> 14f
        FontSize.Medium -> 16f
        FontSize.Large -> 17f
        FontSize.VeryLarge -> 18f
    }

internal val FontSize.subscriptSize: Float
    get() = textSize - 2.2f
