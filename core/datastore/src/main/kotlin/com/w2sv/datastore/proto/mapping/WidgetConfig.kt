package com.w2sv.datastore.proto.mapping

import com.w2sv.datastore.WidgetConfigProto
import com.w2sv.datastore.proto.id_resolving.WifiPropertyProtoRegistry
import com.w2sv.domain.model.widget.WidgetConfig
import com.w2sv.domain.model.widget.WidgetUtility

internal fun WidgetConfig.toProto(): WidgetConfigProto =
    WidgetConfigProto.newBuilder()
        .also { proto ->
            propertyConfigMap.forEach { (property, config) ->
                proto.putProperties(
                    property.protoId,
                    config.toProto()
                )
            }
            proto.addAllOrder(propertyOrder.map { it.protoId })
            proto.appearance = appearance.toProto()
            proto.refreshing = refreshing.toProto()
            utilities.forEach { (element, enabled) ->
                proto.putUtilities(element.ordinal, enabled)
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
        utilities = utilitiesMap.mapKeys { (id, _) ->
            WidgetUtility.entries[id]
        }
    )
