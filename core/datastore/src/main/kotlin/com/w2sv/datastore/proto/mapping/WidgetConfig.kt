package com.w2sv.datastore.proto.mapping

import com.w2sv.datastore.WidgetConfigProto
import com.w2sv.datastore.proto.registry.WifiPropertyProtoRegistry
import com.w2sv.domain.widget.WidgetConfig
import com.w2sv.domain.widget.WidgetUtility
import com.w2sv.domain.wifiproperty.AccessPointNames
import com.w2sv.domain.wifiproperty.WifiProperty

internal fun WidgetConfig.toProto(): WidgetConfigProto =
    WidgetConfigProto.newBuilder()
        .also { builder ->
            propertyConfig.forEach { (property, config) ->
                builder.putProperties(
                    property.protoId,
                    config.toProto()
                )
            }
            builder.addAllOrder(propertyOrder.map { it.protoId })
            builder.appearance = appearance.toProto()
            builder.refreshing = refreshing.toProto()
            utilities.forEach { (element, enabled) ->
                builder.putUtilities(element.protoId, enabled)
            }
            builder.putAllAccessPointNames(accessPointNames.map)
        }
        .build()

internal fun WidgetConfigProto.toExternal(): WidgetConfig =
    WidgetConfig(
        propertyConfig = WidgetConfig.default.propertyConfig + propertiesMap.entries.associate { (id, protoConfig) ->
            WifiPropertyProtoRegistry(id) to protoConfig.toExternal()
        },
        propertyOrder = withMissingPropertiesInserted(orderList.map(WifiPropertyProtoRegistry::invoke)),
        appearance = appearance.toExternal(),
        refreshing = refreshing.toExternal(),
        utilities = utilitiesMap.mapNotNull { (id, enabled) ->
            WidgetUtility.entries.find { it.protoId == id }?.let { it to enabled }
        }.toMap(),
        accessPointNames = AccessPointNames(accessPointNamesMap)
    )

/**
 * Inserts properties which were added in an app update after the config had been persisted, each one right behind its
 * predecessor in [WifiProperty.entries], so that it ends up at a sensible position in the user's custom order.
 */
private fun withMissingPropertiesInserted(order: List<WifiProperty>): List<WifiProperty> {
    if (order.size == WifiProperty.entries.size) return order

    val result = order.toMutableList()
    WifiProperty.entries.forEachIndexed { index, property ->
        if (property !in result) {
            val predecessorIndex = WifiProperty.entries.getOrNull(index - 1)?.let(result::indexOf) ?: -1
            result.add(predecessorIndex + 1, property)
        }
    }
    return result
}
