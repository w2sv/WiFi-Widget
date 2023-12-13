package com.w2sv.domain.repository

import com.w2sv.androidutils.datastorage.datastore.preferences.PersistedValue
import com.w2sv.domain.model.Theme
import com.w2sv.domain.model.WidgetButton
import com.w2sv.domain.model.WidgetColorSection
import com.w2sv.domain.model.WidgetRefreshingParameter
import com.w2sv.domain.model.WidgetWifiProperty
import kotlinx.coroutines.flow.Flow

interface WidgetRepository {
    val theme: PersistedValue.EnumValued<Theme>
    val useDynamicColors: PersistedValue.UniTyped<Boolean>
    val opacity: PersistedValue.UniTyped<Float>

    fun getEnabledWifiProperties(): Set<WidgetWifiProperty>

    fun getCustomColorsMap(): Map<WidgetColorSection, Flow<Int>>
    suspend fun saveCustomColorsMap(map: Map<WidgetColorSection, Int>)

    fun getWifiPropertyEnablementMap(): Map<WidgetWifiProperty, Flow<Boolean>>
    suspend fun saveWifiPropertyEnablementMap(map: Map<WidgetWifiProperty, Boolean>)

    fun getIPSubPropertyEnablementMap(): Map<WidgetWifiProperty.IP.SubProperty, Flow<Boolean>>
    suspend fun saveIPSubPropertyEnablementMap(map: Map<WidgetWifiProperty.IP.SubProperty, Boolean>)

    fun getRefreshingParametersEnablementMap(): Map<WidgetRefreshingParameter, Flow<Boolean>>
    suspend fun saveRefreshingParametersEnablementMap(map: Map<WidgetRefreshingParameter, Boolean>)

    fun getButtonEnablementMap(): Map<WidgetButton, Flow<Boolean>>
    suspend fun saveButtonEnablementMap(map: Map<WidgetButton, Boolean>)
}