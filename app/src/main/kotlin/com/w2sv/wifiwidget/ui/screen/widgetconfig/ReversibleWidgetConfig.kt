package com.w2sv.wifiwidget.ui.screen.widgetconfig

import com.w2sv.domain.model.widget.WifiWidgetConfig
import com.w2sv.domain.model.wifiproperty.WifiProperty
import com.w2sv.kotlinutils.copy
import com.w2sv.kotlinutils.update
import com.w2sv.reversiblestate.ReversibleState
import com.w2sv.reversiblestate.ReversibleStateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

class ReversibleWidgetConfig(reversibleStateFlow: ReversibleStateFlow<WifiWidgetConfig>) :
    ReversibleState by reversibleStateFlow,
    MutableStateFlow<WifiWidgetConfig> by reversibleStateFlow {

    val anyLocationAccessRequiringPropertyEnabled: Boolean
        get() = WifiProperty.locationAccessRequiring.any { value.properties.getValue(it).isEnabled }

    fun updatePropertyEnablement(property: WifiProperty, isEnabled: Boolean) {
        update { config ->
            config.copy(properties = config.properties.copy {
                update(property) { propertyConfig ->
                    propertyConfig.copy(isEnabled = isEnabled)
                }
            })
        }
    }
}
