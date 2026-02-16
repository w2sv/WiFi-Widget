package com.w2sv.domain.repository

import com.w2sv.datastoreutils.datastoreflow.DataStoreFlow
import com.w2sv.datastoreutils.preferences.map.DataStoreFlowMap
import com.w2sv.domain.model.FontSize
import com.w2sv.domain.model.LocationParameter
import com.w2sv.domain.model.PropertyValueAlignment
import com.w2sv.domain.model.WidgetBottomBarElement
import com.w2sv.domain.model.WidgetColoring
import com.w2sv.domain.model.WidgetRefreshing
import com.w2sv.domain.model.WidgetRefreshingParameter
import com.w2sv.domain.model.WifiProperty
import kotlinx.coroutines.flow.Flow
import kotlin.time.Duration

interface WidgetRepository {
    val coloringConfig: Flow<WidgetColoring.Config>
    suspend fun saveColoringConfig(config: WidgetColoring.Config)

    val opacity: DataStoreFlow<Float>
    val fontSize: DataStoreFlow<FontSize>
    val propertyValueAlignment: DataStoreFlow<PropertyValueAlignment>

    val sortedEnabledWifiProperties: Flow<List<WifiProperty>>
    val wifiPropertyOrder: DataStoreFlow<List<WifiProperty>>
    val wifiPropertyEnablementMap: DataStoreFlowMap<WifiProperty, Boolean>

    val enabledIpSubProperties: Flow<Set<WifiProperty.IP.SubProperty>>
    val ipSubPropertyEnablementMap: DataStoreFlowMap<WifiProperty.IP.SubProperty, Boolean>

    val bottomRowElementEnablementMap: DataStoreFlowMap<WidgetBottomBarElement, Boolean>
    val refreshingParametersEnablementMap: DataStoreFlowMap<WidgetRefreshingParameter, Boolean>
    val refreshInterval: DataStoreFlow<Duration>
    val refreshing: Flow<WidgetRefreshing>

    val locationParameters: DataStoreFlowMap<LocationParameter, Boolean>
    val enabledLocationParameters: Flow<Set<LocationParameter>>
}
