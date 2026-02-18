package com.w2sv.wifiwidget.ui.screens.widgetconfiguration.model

import androidx.annotation.StringRes
import com.w2sv.core.domain.R
import com.w2sv.domain.model.Labelled

enum class WidgetRefreshingParameter(@StringRes override val labelRes: Int, val defaultIsEnabled: Boolean) :
    Labelled {
    RefreshPeriodically(R.string.refresh_periodically, true),
    RefreshOnLowBattery(R.string.refresh_on_low_battery, true)
}
