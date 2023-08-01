package com.w2sv.common.data.model

enum class WidgetRefreshingParameter {
    RefreshPeriodically,
    RefreshOnLowBattery,
    DisplayLastRefreshDateTime
}

data class WidgetRefreshing(
    val refreshPeriodically: Boolean,
    val refreshOnLowBattery: Boolean
)