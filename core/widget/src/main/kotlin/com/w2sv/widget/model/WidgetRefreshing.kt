package com.w2sv.widget.model

import com.w2sv.domain.model.WidgetRefreshingParameter
import kotlin.time.Duration

internal data class WidgetRefreshing(val refreshPeriodically: Boolean, val refreshOnLowBattery: Boolean, val refreshInterval: Duration) {
    constructor(parameters: Map<WidgetRefreshingParameter, Boolean>, interval: Duration) : this(
        refreshPeriodically = parameters.getValue(WidgetRefreshingParameter.RefreshPeriodically),
        refreshOnLowBattery = parameters.getValue(WidgetRefreshingParameter.RefreshOnLowBattery),
        refreshInterval = interval
    )
}
