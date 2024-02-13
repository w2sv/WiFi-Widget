package com.w2sv.domain.repository

import com.w2sv.androidutils.datastorage.datastore.DataStoreStateFlow
import com.w2sv.domain.model.Theme
import com.w2sv.domain.model.WidgetBottomBarElement
import com.w2sv.domain.model.WidgetColorSection
import com.w2sv.domain.model.WidgetRefreshingParameter
import com.w2sv.domain.model.WidgetWifiProperty
import kotlinx.coroutines.flow.StateFlow

interface WidgetRepository {
    val theme: DataStoreStateFlow<Theme>
    val useDynamicColors: DataStoreStateFlow<Boolean>
    val opacity: DataStoreStateFlow<Float>

    val customColorsMap: Map<WidgetColorSection, StateFlow<Int>>
    suspend fun saveCustomColorsMap(map: Map<WidgetColorSection, Int>)

    val wifiPropertyEnablementMap: Map<WidgetWifiProperty, StateFlow<Boolean>>
    suspend fun saveWifiPropertyEnablementMap(map: Map<WidgetWifiProperty, Boolean>)

    val ipSubPropertyEnablementMap: Map<WidgetWifiProperty.IP.SubProperty, StateFlow<Boolean>>
    suspend fun saveIPSubPropertyEnablementMap(map: Map<WidgetWifiProperty.IP.SubProperty, Boolean>)

    val refreshingParametersEnablementMap: Map<WidgetRefreshingParameter, StateFlow<Boolean>>
    suspend fun saveRefreshingParametersEnablementMap(map: Map<WidgetRefreshingParameter, Boolean>)

    val bottomBarElementEnablementMap: Map<WidgetBottomBarElement, StateFlow<Boolean>>
    suspend fun saveBottomBarElementEnablementMap(map: Map<WidgetBottomBarElement, Boolean>)
}