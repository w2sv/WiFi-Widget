package com.w2sv.wifiwidget.ui.screens.widgetconfiguration.model

import androidx.compose.runtime.Stable
import com.w2sv.domain.model.widget.WifiWidgetConfig
import com.w2sv.domain.model.wifiproperty.WifiProperty
import com.w2sv.domain.repository.WidgetConfigDataSource
import com.w2sv.kotlinutils.copy
import com.w2sv.kotlinutils.coroutines.flow.mapState
import com.w2sv.kotlinutils.update
import com.w2sv.reversiblestate.ReversibleState
import com.w2sv.reversiblestate.ReversibleStateFlow
import com.w2sv.wifiwidget.ui.screens.home.components.OnLocationAccessGrant
import com.w2sv.wifiwidget.ui.screens.home.components.OnLocationAccessGrant.EnableLocationAccessDependentProperties
import com.w2sv.wifiwidget.ui.screens.home.components.OnLocationAccessGrant.EnableProperty
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@Stable
class ReversibleWidgetConfig(reversibleStateFlow: ReversibleStateFlow<WifiWidgetConfig>, private val scope: CoroutineScope) :
    ReversibleState by reversibleStateFlow,
    MutableStateFlow<WifiWidgetConfig> by reversibleStateFlow {

    constructor(
        scope: CoroutineScope,
        dataSource: WidgetConfigDataSource,
        onStateSynced: suspend () -> Unit
    ) : this(
        reversibleStateFlow = ReversibleStateFlow(
            scope = scope,
            appliedStateFlow = dataSource.config.stateIn(
                scope = scope,
                started = SharingStarted.WhileSubscribed(),
                initialValue = WifiWidgetConfig.default
            ),
            syncState = {
                dataSource.update(it)
                onStateSynced()
            }
        ),
        scope = scope
    )

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

    fun onLocationAccessPermissionGranted(onGrantAction: OnLocationAccessGrant) {
        when (onGrantAction) {
            is EnableLocationAccessDependentProperties -> {
                WifiProperty.locationAccessRequiring.forEach { property ->
                    updatePropertyEnablement(property, true)
                }
                scope.launch { sync() }
            }

            is EnableProperty -> updatePropertyEnablement(onGrantAction.property, true)
            else -> Unit
        }
    }

    fun restoreDefaultPropertyOrder() {
        update { it.copy(orderedProperties = WifiProperty.entries) }
    }

    val propertiesInDefaultOrder: StateFlow<Boolean> = mapState { it.orderedProperties == WifiProperty.entries }
}
