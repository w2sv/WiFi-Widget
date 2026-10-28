package com.w2sv.wifiwidget.ui.screen.widgetconfig.model

import androidx.annotation.StringRes
import com.w2sv.core.resources.R
import com.w2sv.domain.WithLabel

enum class WidgetRefreshingParameter(@StringRes override val labelRes: Int) : WithLabel {
    RefreshPeriodically(R.string.refresh_periodically),
    Interval(R.string.interval)
}
