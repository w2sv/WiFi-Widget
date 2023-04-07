package com.w2sv.common

import androidx.annotation.StringRes

enum class WidgetColorSection(@StringRes val labelRes: Int) {
    Background(R.string.background),
    Labels(R.string.labels),
    Values(R.string.values)
}