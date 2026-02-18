package com.w2sv.datastore.proto.mapping

import com.w2sv.datastore.WifiWidgetConfigProto
import com.w2sv.datastore.proto.id_resolving.WifiPropertyProtoRegistry
import com.w2sv.domain.model.widget.WidgetBottomBarElement
import com.w2sv.domain.model.widget.WifiWidgetConfig

internal fun WifiWidgetConfig.toProto(): WifiWidgetConfigProto =
    WifiWidgetConfigProto.newBuilder()
        .also { proto ->
            properties.forEach { (property, config) ->
                proto.putProperties(
                    property.protoId,
                    config.toProto()
                )
            }
            proto.addAllOrder(orderedProperties.map { it.protoId })
            proto.appearance = appearance.toProto()
            proto.refreshing = refreshing.toProto()
            bottomBarElements.forEach { (element, enabled) ->
                proto.putBottomBarElements(element.ordinal, enabled)
            }
        }
        .build()

internal fun WifiWidgetConfigProto.toExternal(): WifiWidgetConfig =
    WifiWidgetConfig(
        properties = propertiesMap.entries.associate { (id, protoConfig) ->
            WifiPropertyProtoRegistry(id) to protoConfig.toExternal()
        },
        orderedProperties = orderList.map(WifiPropertyProtoRegistry::invoke),
        appearance = appearance.toExternal(),
        refreshing = refreshing.toExternal(),
        bottomBarElements = bottomBarElementsMap.mapKeys { (id, _) ->
            WidgetBottomBarElement.entries[id]
        }
    )
