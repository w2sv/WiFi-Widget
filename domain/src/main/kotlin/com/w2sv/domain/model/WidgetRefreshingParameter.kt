package com.w2sv.domain.model

enum class WidgetRefreshingParameter(val defaultIsEnabled: Boolean) {
    RefreshPeriodically(true),
    RefreshOnLowBattery(false),
    DisplayLastRefreshDateTime(true)
}
