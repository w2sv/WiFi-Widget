package com.w2sv.domain.repository

import com.w2sv.androidutils.datastorage.datastore.DataStoreStateFlow
import com.w2sv.domain.model.FontSize
import com.w2sv.domain.model.WidgetBottomRowElement
import com.w2sv.domain.model.WidgetColoring
import com.w2sv.domain.model.WidgetRefreshingParameter
import com.w2sv.domain.model.WidgetWifiProperty
import kotlinx.coroutines.flow.StateFlow

interface WidgetRepository {
    val coloringConfig: StateFlow<WidgetColoring.Config>
    suspend fun saveColoringConfig(config: WidgetColoring.Config)

    val opacity: DataStoreStateFlow<Float>
    val fontSize: DataStoreStateFlow<FontSize>

    val wifiPropertyEnablementMap: Map<WidgetWifiProperty, StateFlow<Boolean>>
    suspend fun saveWifiPropertyEnablementMap(map: Map<WidgetWifiProperty, Boolean>)

    val ipSubPropertyEnablementMap: Map<WidgetWifiProperty.IP.SubProperty, StateFlow<Boolean>>
    suspend fun saveIPSubPropertyEnablementMap(map: Map<WidgetWifiProperty.IP.SubProperty, Boolean>)

    val refreshingParametersEnablementMap: Map<WidgetRefreshingParameter, StateFlow<Boolean>>
    suspend fun saveRefreshingParametersEnablementMap(map: Map<WidgetRefreshingParameter, Boolean>)

    val bottomRowElementEnablementMap: Map<WidgetBottomRowElement, StateFlow<Boolean>>
    suspend fun saveBottomRowElementEnablementMap(map: Map<WidgetBottomRowElement, Boolean>)
}