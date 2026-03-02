package com.w2sv.wifiwidget.ui.screen.widgetconfig.model

import androidx.annotation.StringRes
import com.w2sv.core.domain.R
import com.w2sv.domain.model.Labelled

enum class WidgetRefreshingParameter(@StringRes override val labelRes: Int) :
    Labelled {
    RefreshPeriodically(R.string.refresh_periodically),
    RefreshOnLowBattery(R.string.refresh_on_low_battery)
}
