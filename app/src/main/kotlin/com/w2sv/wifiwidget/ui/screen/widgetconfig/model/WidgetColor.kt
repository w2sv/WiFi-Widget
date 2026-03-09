package com.w2sv.wifiwidget.ui.screen.widgetconfig.model

import androidx.annotation.StringRes
import com.w2sv.core.common.R

enum class WidgetColor(@StringRes val labelRes: Int) {
    Background(R.string.background),
    Primary(R.string.primary),
    Secondary(R.string.secondary)
}
