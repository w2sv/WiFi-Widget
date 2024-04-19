package com.w2sv.widget.model

import com.w2sv.domain.model.WidgetRefreshingParameter

data class WidgetRefreshing(
    val refreshPeriodically: Boolean,
    val refreshOnLowBattery: Boolean,
) {
    constructor(parameters: Map<WidgetRefreshingParameter, Boolean>) : this(
        refreshPeriodically = parameters.getValue(WidgetRefreshingParameter.RefreshPeriodically),
        refreshOnLowBattery = parameters.getValue(WidgetRefreshingParameter.RefreshOnLowBattery)
    )
}
