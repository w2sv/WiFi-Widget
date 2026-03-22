package com.w2sv.datastore.proto.mapping

import com.w2sv.datastore.WidgetConfigProto
import com.w2sv.datastore.proto.id_resolving.WifiPropertyProtoRegistry
import com.w2sv.domain.model.widget.WidgetConfig
import com.w2sv.domain.model.widget.WidgetUtility

internal fun WidgetConfig.toProto(): WidgetConfigProto =
    WidgetConfigProto.newBuilder()
        .also { builder ->
            propertyConfigMap.forEach { (property, config) ->
                builder.putProperties(
                    property.protoId,
                    config.toProto()
                )
            }
            builder.addAllOrder(propertyOrder.map { it.protoId })
            builder.appearance = appearance.toProto()
            builder.refreshing = refreshing.toProto()
            utilities.forEach { (element, enabled) ->
                builder.putUtilities(element.ordinal, enabled)
            }
        }
        .build()

internal fun WidgetConfigProto.toExternal(): WidgetConfig =
    WidgetConfig(
        propertyConfigMap = propertiesMap.entries.associate { (id, protoConfig) ->
            WifiPropertyProtoRegistry(id) to protoConfig.toExternal()
        },
        propertyOrder = orderList.map(WifiPropertyProtoRegistry::invoke),
        appearance = appearance.toExternal(),
        refreshing = refreshing.toExternal(),
        utilities = utilitiesMap.mapKeys { (id, _) -> WidgetUtility.entries[id] }
    )
