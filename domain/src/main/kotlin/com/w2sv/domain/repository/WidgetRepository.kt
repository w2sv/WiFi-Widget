package com.w2sv.domain.repository

import com.w2sv.androidutils.datastorage.datastore.DataStoreStateFlow
import com.w2sv.domain.model.WidgetBottomRowElement
import com.w2sv.domain.model.WidgetColoring
import com.w2sv.domain.model.WidgetRefreshingParameter
import com.w2sv.domain.model.WidgetWifiProperty
import kotlinx.coroutines.flow.StateFlow

interface WidgetRepository {
    val coloring: DataStoreStateFlow<WidgetColoring>

    val presetColoringData: StateFlow<WidgetColoring.Data.Preset>
    suspend fun savePresetColoringData(data: WidgetColoring.Data.Preset)

    val customColoringData: StateFlow<WidgetColoring.Data.Custom>
    suspend fun saveCustomColoringData(data: WidgetColoring.Data.Custom)

    val opacity: DataStoreStateFlow<Float>

    val wifiPropertyEnablementMap: Map<WidgetWifiProperty, StateFlow<Boolean>>
    suspend fun saveWifiPropertyEnablementMap(map: Map<WidgetWifiProperty, Boolean>)

    val ipSubPropertyEnablementMap: Map<WidgetWifiProperty.IP.SubProperty, StateFlow<Boolean>>
    suspend fun saveIPSubPropertyEnablementMap(map: Map<WidgetWifiProperty.IP.SubProperty, Boolean>)

    val refreshingParametersEnablementMap: Map<WidgetRefreshingParameter, StateFlow<Boolean>>
    suspend fun saveRefreshingParametersEnablementMap(map: Map<WidgetRefreshingParameter, Boolean>)

    val bottomRowElementEnablementMap: Map<WidgetBottomRowElement, StateFlow<Boolean>>
    suspend fun saveBottomRowElementEnablementMap(map: Map<WidgetBottomRowElement, Boolean>)
}