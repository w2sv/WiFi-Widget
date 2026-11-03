package com.w2sv.datastore.proto.mapping

import com.w2sv.datastore.WidgetRefreshingProto
import com.w2sv.domain.widget.WidgetRefreshing
import kotlin.time.Duration.Companion.minutes

internal fun WidgetRefreshing.toProto(): WidgetRefreshingProto =
    WidgetRefreshingProto.newBuilder()
        .setRefreshPeriodically(refreshPeriodically)
        .setRefreshIntervalMinutes(interval.inWholeMinutes)
        .build()

internal fun WidgetRefreshingProto.toExternal(): WidgetRefreshing =
    WidgetRefreshing(
        refreshPeriodically = refreshPeriodically,
        interval = refreshIntervalMinutes.minutes
    )
