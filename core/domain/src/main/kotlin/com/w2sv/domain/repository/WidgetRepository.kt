package com.w2sv.domain.repository

import com.w2sv.datastoreutils.datastoreflow.DataStoreFlow
import com.w2sv.datastoreutils.preferences.map.DataStoreFlowMap
import com.w2sv.domain.model.FontSize
import com.w2sv.domain.model.LocationParameter
import com.w2sv.domain.model.PropertyValueAlignment
import com.w2sv.domain.model.WidgetBottomBarElement
import com.w2sv.domain.model.WidgetColoring
import com.w2sv.domain.model.WidgetRefreshingParameter
import com.w2sv.domain.model.WifiProperty
import kotlin.time.Duration
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface WidgetRepository {
    val coloringConfig: Flow<WidgetColoring.Config>
    suspend fun saveColoringConfig(config: WidgetColoring.Config)

    val opacity: DataStoreFlow<Float>
    val fontSize: DataStoreFlow<FontSize>
    val propertyValueAlignment: DataStoreFlow<PropertyValueAlignment>

    val sortedEnabledWifiProperties: StateFlow<List<WifiProperty>>
    val wifiPropertyOrder: DataStoreFlow<List<WifiProperty>>
    val wifiPropertyEnablementMap: DataStoreFlowMap<WifiProperty, Boolean>

    val enabledIpSubProperties: StateFlow<Set<WifiProperty.IP.SubProperty>>
    val ipSubPropertyEnablementMap: DataStoreFlowMap<WifiProperty.IP.SubProperty, Boolean>

    val bottomRowElementEnablementMap: DataStoreFlowMap<WidgetBottomBarElement, Boolean>
    val refreshingParametersEnablementMap: DataStoreFlowMap<WidgetRefreshingParameter, Boolean>
    val refreshInterval: DataStoreFlow<Duration>

    val locationParameters: DataStoreFlowMap<LocationParameter, Boolean>
    val enabledLocationParameters: StateFlow<Set<LocationParameter>>
}
