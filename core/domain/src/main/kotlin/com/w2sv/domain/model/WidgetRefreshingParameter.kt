package com.w2sv.domain.model

import androidx.annotation.StringRes
import com.w2sv.core.domain.R

enum class WidgetRefreshingParameter(
    @all:StringRes override val labelRes: Int,
    val defaultIsEnabled: Boolean
) :
    WidgetProperty {
    RefreshPeriodically(R.string.refresh_periodically, true),
    RefreshOnLowBattery(R.string.refresh_on_low_battery, true)
}
