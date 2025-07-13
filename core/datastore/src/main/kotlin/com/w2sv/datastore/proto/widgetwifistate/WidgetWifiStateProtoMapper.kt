package com.w2sv.datastore.proto.widgetwifistate

import com.w2sv.datastore.WidgetWifiStateProto
import com.w2sv.datastore.WifiPropertyProto
import com.w2sv.datastore.WifiStatusProto
import com.w2sv.datastore.proto.Mapper
import com.w2sv.datastore.widgetWifiStateProto
import com.w2sv.datastore.wifiPropertyProto
import com.w2sv.domain.model.WidgetWifiState
import com.w2sv.domain.model.WifiProperty

internal object WidgetWifiStateProtoMapper : Mapper<WidgetWifiStateProto, WidgetWifiState> {

    override fun toExternal(proto: WidgetWifiStateProto): WidgetWifiState =
        when (proto.type) {
            WifiStatusProto.NO_CONNECTION -> WidgetWifiState.Disconnected
            WifiStatusProto.PROPERTIES_LOADING -> WidgetWifiState.Connected.PropertiesLoading
            WifiStatusProto.PROPERTIES_AVAILABLE -> {
                WidgetWifiState.Connected.PropertiesAvailable(proto.propertiesList.map { it.toViewData() })
            }

            else -> WidgetWifiState.Disabled
        }

    override fun toProto(external: WidgetWifiState): WidgetWifiStateProto =
        widgetWifiStateProto {
            when (external) {
                WidgetWifiState.Disabled -> type = WifiStatusProto.DISABLED
                WidgetWifiState.Disconnected -> type = WifiStatusProto.NO_CONNECTION
                WidgetWifiState.Connected.PropertiesLoading -> type = WifiStatusProto.PROPERTIES_LOADING
                is WidgetWifiState.Connected.PropertiesAvailable -> {
                    type = WifiStatusProto.PROPERTIES_AVAILABLE
                    properties.addAll(external.properties.map { it.toProto() })
                }
            }
        }
}

/** Extension to map proto Property to ViewData */
private fun WifiPropertyProto.toViewData(): WifiProperty.ViewData =
    if (subPropertyValuesList.isEmpty()) {
        WifiProperty.ViewData.NonIP(label, value)
    } else {
        WifiProperty.ViewData.IPProperty(label, value, subPropertyValuesList)
    }

/** Extension to map ViewData to proto Property */
private fun WifiProperty.ViewData.toProto(): WifiPropertyProto =
    wifiPropertyProto {
        label = this@toProto.label
        value = this@toProto.value
        if (this@toProto is WifiProperty.ViewData.IPProperty) {
            subPropertyValues.addAll(nonEmptySubPropertyValuesOrNull ?: emptyList())
        }
    }

