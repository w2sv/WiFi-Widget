package com.w2sv.domain.model.widget

import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

data class WidgetRefreshing(
    val refreshPeriodically: Boolean = true,
    val refreshOnLowBattery: Boolean = true,
    val refreshInterval: Duration = 15.minutes
)
