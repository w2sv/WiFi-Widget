package com.w2sv.domain.model

import kotlin.time.Duration

data class WidgetRefreshing(val refreshPeriodically: Boolean, val refreshOnLowBattery: Boolean, val refreshInterval: Duration)
