package com.w2sv.common

import androidx.annotation.StringRes

enum class CustomizableWidgetSection(@StringRes val labelRes: Int) {
    Background(R.string.background),
    Labels(R.string.labels),
    Values(R.string.values)
}