package com.w2sv.datastore.proto.migration

import com.w2sv.datastore.proto.mapping.toProto
import com.w2sv.domain.model.widget.WidgetConfig

internal fun defaultWidgetColoringProto() =
    WidgetConfig.default.appearance.coloring.toProto()
