package com.w2sv.datastore.proto.widgetcoloring

import com.w2sv.domain.model.WidgetColoring

internal val defaultWidgetColoringProto =
    WidgetColoringConfigMapper.toProto(WidgetColoring.Config())
