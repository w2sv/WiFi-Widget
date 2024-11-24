package com.w2sv.domain.repository

import com.w2sv.datastoreutils.datastoreflow.DataStoreFlow
import com.w2sv.datastoreutils.preferences.map.DataStoreFlowMap
import com.w2sv.domain.model.FontSize
import com.w2sv.domain.model.WidgetBottomBarElement
import com.w2sv.domain.model.WidgetColoring
import com.w2sv.domain.model.WidgetRefreshingParameter
import com.w2sv.domain.model.WifiProperty
import kotlin.time.Duration
import kotlinx.coroutines.flow.Flow

interface WidgetRepository {
    val coloringConfig: Flow<WidgetColoring.Config>
    suspend fun saveColoringConfig(config: WidgetColoring.Config)

    val opacity: DataStoreFlow<Float>
    val fontSize: DataStoreFlow<FontSize>

    val sortedEnabledWifiProperties: Flow<List<WifiProperty>>
    val wifiPropertyEnablementMap: DataStoreFlowMap<WifiProperty, Boolean>
    val orderedWifiProperties: DataStoreFlow<List<WifiProperty>>
    val ipSubPropertyEnablementMap: DataStoreFlowMap<WifiProperty.IP.SubProperty, Boolean>
    val bottomRowElementEnablementMap: DataStoreFlowMap<WidgetBottomBarElement, Boolean>
    val refreshingParametersEnablementMap: DataStoreFlowMap<WidgetRefreshingParameter, Boolean>
    val refreshInterval: DataStoreFlow<Duration>
}
