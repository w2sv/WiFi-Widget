package com.w2sv.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import com.w2sv.androidutils.datastorage.datastore.DataStoreEntry
import com.w2sv.androidutils.datastorage.datastore.DataStoreRepository
import com.w2sv.common.utils.dynamicColorsSupported
import com.w2sv.data.model.isEnabledDSE
import com.w2sv.data.model.isEnabledDse
import com.w2sv.data.model.valueDSE
import com.w2sv.domain.model.Theme
import com.w2sv.domain.model.WidgetBottomBarElement
import com.w2sv.domain.model.WidgetColorSection
import com.w2sv.domain.model.WidgetRefreshingParameter
import com.w2sv.domain.model.WidgetWifiProperty
import com.w2sv.domain.repository.WidgetRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WidgetRepositoryImpl @Inject constructor(
    dataStore: DataStore<Preferences>,
) : DataStoreRepository(dataStore),
    WidgetRepository {

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    // ================
    // PersistedValue
    // ================

    override val theme = dataStoreStateFlow(
        key = intPreferencesKey("widgetTheme"),
        default = Theme.SystemDefault,
        scope = scope,
        sharingStarted = SharingStarted.Eagerly
    )

    override val useDynamicColors =
        dataStoreStateFlow(
            key = booleanPreferencesKey("widgetConfiguration.useDynamicColor"),
            default = dynamicColorsSupported,
            scope = scope,
            sharingStarted = SharingStarted.Eagerly
        )

    override val opacity = dataStoreStateFlow(
        key = floatPreferencesKey("opacity"),
        default = 1.0f,
        scope = scope,
        sharingStarted = SharingStarted.Eagerly
    )

    // ================
    // Map
    // ================

    override val customColorsMap: Map<WidgetColorSection, StateFlow<Int>> =
        getStateFlowMap(WidgetColorSection.entries.associateBy { it.valueDSE })

    override suspend fun saveCustomColorsMap(map: Map<WidgetColorSection, Int>) {
        saveMap(map.mapKeys { (k, _) -> k.valueDSE })
    }

    override val wifiPropertyEnablementMap: Map<WidgetWifiProperty, StateFlow<Boolean>> =
        getStateFlowMap(WidgetWifiProperty.entries.associateBy { it.isEnabledDSE })

    override suspend fun saveWifiPropertyEnablementMap(map: Map<WidgetWifiProperty, Boolean>) {
        saveMap(map.mapKeys { (k, _) -> k.isEnabledDSE })
    }

    override val ipSubPropertyEnablementMap: Map<WidgetWifiProperty.IP.SubProperty, StateFlow<Boolean>> =
        getStateFlowMap(
            WidgetWifiProperty.IP.entries
                .flatMap { it.subProperties }
                .associateBy { it.isEnabledDse }
        )

    override suspend fun saveIPSubPropertyEnablementMap(map: Map<WidgetWifiProperty.IP.SubProperty, Boolean>) {
        saveMap(map.mapKeys { (k, _) -> k.isEnabledDse })
    }

    override val refreshingParametersEnablementMap: Map<WidgetRefreshingParameter, StateFlow<Boolean>> =
        getStateFlowMap(WidgetRefreshingParameter.entries.associateBy { it.isEnabledDSE })

    override suspend fun saveRefreshingParametersEnablementMap(map: Map<WidgetRefreshingParameter, Boolean>) {
        saveMap(map.mapKeys { (k, _) -> k.isEnabledDSE })
    }

    override val bottomBarElementEnablementMap: Map<WidgetBottomBarElement, StateFlow<Boolean>> =
        getStateFlowMap(WidgetBottomBarElement.entries.associateBy { it.isEnabledDSE })

    override suspend fun saveBottomBarElementEnablementMap(map: Map<WidgetBottomBarElement, Boolean>) {
        saveMap(map.mapKeys { (k, _) -> k.isEnabledDSE })
    }

    private fun <E, V> getStateFlowMap(dseToType: Map<DataStoreEntry.UniType<V>, E>): Map<E, StateFlow<V>> =
        getStateFlowMap(
            properties = dseToType.keys,
            scope = scope,
            sharingStarted = SharingStarted.Eagerly
        )
            .mapKeys { (k, _) -> dseToType.getValue(k) }
}
