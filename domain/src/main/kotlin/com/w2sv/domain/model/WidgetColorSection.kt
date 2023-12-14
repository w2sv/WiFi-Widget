package com.w2sv.domain.model

import androidx.annotation.ColorInt
import androidx.annotation.StringRes
import com.w2sv.domain.R

enum class WidgetColorSection(
    @StringRes override val labelRes: Int,
    @ColorInt val defaultValue: Int
) : WidgetProperty {
    Background(R.string.background, -7859146),
    Primary(R.string.primary, -5898336),
    Secondary(R.string.secondary, -1),
}
