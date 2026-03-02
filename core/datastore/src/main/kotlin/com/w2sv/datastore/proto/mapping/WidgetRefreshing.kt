package com.w2sv.datastore.proto.mapping

import com.w2sv.datastore.WidgetRefreshingProto
import com.w2sv.domain.model.widget.WidgetRefreshing
import kotlin.time.Duration.Companion.minutes

internal fun WidgetRefreshing.toProto(): WidgetRefreshingProto =
    WidgetRefreshingProto.newBuilder()
        .setRefreshPeriodically(refreshPeriodically)
        .setRefreshOnLowBattery(refreshOnLowBattery)
        .setRefreshIntervalMinutes(interval.inWholeMinutes)
        .build()

internal fun WidgetRefreshingProto.toExternal(): WidgetRefreshing =
    WidgetRefreshing(
        refreshPeriodically = refreshPeriodically,
        refreshOnLowBattery = refreshOnLowBattery,
        interval = refreshIntervalMinutes.minutes
    )
