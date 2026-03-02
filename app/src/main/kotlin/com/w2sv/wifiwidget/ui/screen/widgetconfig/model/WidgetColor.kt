package com.w2sv.wifiwidget.ui.screen.widgetconfig.model

import androidx.annotation.StringRes

enum class WidgetColor(@StringRes val labelRes: Int) {
    Background(com.w2sv.core.domain.R.string.background),
    Primary(com.w2sv.core.domain.R.string.primary),
    Secondary(com.w2sv.core.domain.R.string.secondary)
}
