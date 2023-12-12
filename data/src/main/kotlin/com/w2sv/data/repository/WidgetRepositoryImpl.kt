package com.w2sv.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import com.w2sv.androidutils.coroutines.getSynchronousMap
import com.w2sv.androidutils.datastorage.datastore.preferences.DataStoreEntry
import com.w2sv.androidutils.datastorage.datastore.preferences.PreferencesDataStoreRepository
import com.w2sv.common.utils.dynamicColorsSupported
import com.w2sv.data.model.isEnabledDSE
import com.w2sv.data.model.isEnabledDse
import com.w2sv.data.model.valueDSE
import com.w2sv.domain.model.Theme
import com.w2sv.domain.model.WidgetButton
import com.w2sv.domain.model.WidgetColorSection
import com.w2sv.domain.model.WidgetRefreshingParameter
import com.w2sv.domain.model.WidgetWifiProperty
import com.w2sv.domain.repository.WidgetRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WidgetRepositoryImpl @Inject constructor(
    dataStore: DataStore<Preferences>,
) : PreferencesDataStoreRepository(dataStore),
    WidgetRepository {

    override val optionsChangedWidgetId get() = _optionsChangedWidgetId.asSharedFlow()
    private val _optionsChangedWidgetId = MutableSharedFlow<Int>()

    override fun onWidgetOptionsChanged(widgetId: Int) {
        CoroutineScope(Dispatchers.Default).launch {
            _optionsChangedWidgetId.emit(widgetId)
        }
    }

    override fun getEnabledWifiProperties(): Set<WidgetWifiProperty> =
        getWifiPropertyEnablementMap().getSynchronousMap().filterValues { it }.keys

    // ================
    // PersistedValue
    // ================

    override val theme = getPersistedValue(intPreferencesKey("widgetTheme"), Theme.SystemDefault)

    override val useDynamicColors =
        getPersistedValue(
            booleanPreferencesKey("widgetConfiguration.useDynamicColor"),
            dynamicColorsSupported
        )

    override val opacity = getPersistedValue(floatPreferencesKey("opacity"), 1.0f)

    // ================
    // Map
    // ================

    // TODO: look for singular save function

    override fun getCustomColorsMap(): Map<WidgetColorSection, Flow<Int>> =
        getTypeToValueMap(WidgetColorSection.entries.associateBy { it.valueDSE })

    override suspend fun saveCustomColorsMap(map: Map<WidgetColorSection, Int>) {
        saveMap(map.mapKeys { (k, _) -> k.valueDSE })
    }

    override fun getWifiPropertyEnablementMap(): Map<WidgetWifiProperty, Flow<Boolean>> =
        getTypeToValueMap(WidgetWifiProperty.entries.associateBy { it.isEnabledDSE })

    override suspend fun saveWifiPropertyEnablementMap(map: Map<WidgetWifiProperty, Boolean>) {
        saveMap(map.mapKeys { (k, _) -> k.isEnabledDSE })
    }

    override fun getIPSubPropertyEnablementMap(): Map<WidgetWifiProperty.IP.SubProperty, Flow<Boolean>> =
        getTypeToValueMap(
            WidgetWifiProperty.IP.entries
                .flatMap { it.subProperties }
                .associateBy { it.isEnabledDse }
        )

    override suspend fun saveIPSubPropertyEnablementMap(map: Map<WidgetWifiProperty.IP.SubProperty, Boolean>) {
        saveMap(map.mapKeys { (k, _) -> k.isEnabledDse })
    }

    override fun getRefreshingParametersEnablementMap(): Map<WidgetRefreshingParameter, Flow<Boolean>> =
        getTypeToValueMap(WidgetRefreshingParameter.entries.associateBy { it.isEnabledDSE })

    override suspend fun saveRefreshingParametersEnablementMap(map: Map<WidgetRefreshingParameter, Boolean>) {
        saveMap(map.mapKeys { (k, _) -> k.isEnabledDSE })
    }

    override fun getButtonEnablementMap(): Map<WidgetButton, Flow<Boolean>> =
        getTypeToValueMap(WidgetButton.entries.associateBy { it.isEnabledDSE })

    override suspend fun saveButtonEnablementMap(map: Map<WidgetButton, Boolean>) {
        saveMap(map.mapKeys { (k, _) -> k.isEnabledDSE })
    }

    private fun <E, V> getTypeToValueMap(dseToType: Map<DataStoreEntry.UniType<V>, E>): Map<E, Flow<V>> =
        getFlowMap(dseToType.keys).mapKeys { (k, _) -> dseToType.getValue(k) }
}
