package com.w2sv.domain.widget

import androidx.annotation.StringRes
import com.w2sv.core.resources.R

enum class FontSize(@StringRes val labelRes: Int) {
    VerySmall(labelRes = R.string.very_small),
    Small(labelRes = R.string.small),
    Medium(labelRes = R.string.medium),
    Large(labelRes = R.string.large),
    VeryLarge(labelRes = R.string.very_large)
}
