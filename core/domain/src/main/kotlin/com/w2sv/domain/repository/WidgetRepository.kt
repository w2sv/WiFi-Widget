package com.w2sv.domain.repository

import com.w2sv.androidutils.datastorage.preferences_datastore.flow.DataStoreFlow
import com.w2sv.androidutils.datastorage.preferences_datastore.flow.DataStoreFlowMap
import com.w2sv.domain.model.FontSize
import com.w2sv.domain.model.WidgetBottomRowElement
import com.w2sv.domain.model.WidgetColoring
import com.w2sv.domain.model.WidgetRefreshingParameter
import com.w2sv.domain.model.WidgetWifiProperty
import kotlinx.coroutines.flow.Flow

interface WidgetRepository {
    val coloringConfig: Flow<WidgetColoring.Config>
    suspend fun saveColoringConfig(config: WidgetColoring.Config)

    val opacity: DataStoreFlow<Float>
    val fontSize: DataStoreFlow<FontSize>

    val wifiPropertyEnablementMap: DataStoreFlowMap<WidgetWifiProperty, Boolean>
    val ipSubPropertyEnablementMap: DataStoreFlowMap<WidgetWifiProperty.IP.SubProperty, Boolean>
    val refreshingParametersEnablementMap: DataStoreFlowMap<WidgetRefreshingParameter, Boolean>
    val bottomRowElementEnablementMap: DataStoreFlowMap<WidgetBottomRowElement, Boolean>
}