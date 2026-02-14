package com.w2sv.domain.model

import androidx.annotation.StringRes
import com.w2sv.core.domain.R

enum class FontSize(@StringRes val labelRes: Int, val value: Float) {
    VerySmall(labelRes = R.string.very_small, value = 12f),
    Small(labelRes = R.string.small, value = 14f),
    Medium(labelRes = R.string.medium, value = 16f),
    Large(labelRes = R.string.large, value = 17f),
    VeryLarge(labelRes = R.string.very_large, value = 18f);

    val subscriptSize: Float by lazy { value - 2f }
}
