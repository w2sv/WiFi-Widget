package com.w2sv.datastore.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import com.w2sv.androidutils.datastorage.datastore.DataStoreEntry
import com.w2sv.androidutils.datastorage.datastore.DataStoreRepository
import com.w2sv.datastore.proto.widget_coloring.WidgetColoringDataSource
import com.w2sv.domain.model.FontSize
import com.w2sv.domain.model.WidgetBottomRowElement
import com.w2sv.domain.model.WidgetColoring
import com.w2sv.domain.model.WidgetRefreshingParameter
import com.w2sv.domain.model.WidgetWifiProperty
import com.w2sv.domain.repository.WidgetRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WidgetRepositoryImpl @Inject constructor(
    dataStore: DataStore<Preferences>,
    private val widgetColoringDataSource: WidgetColoringDataSource
) : DataStoreRepository(dataStore),
    WidgetRepository {

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    override val coloringConfig: StateFlow<WidgetColoring.Config> =
        widgetColoringDataSource.config.stateIn(
            scope = scope,
            started = SharingStarted.Eagerly,
            initialValue = WidgetColoring.Config()
        )

    override suspend fun saveColoringConfig(config: WidgetColoring.Config) {
        widgetColoringDataSource.saveConfig(config)
    }

    override val opacity = dataStoreStateFlow(
        key = floatPreferencesKey("opacity"),
        default = 1.0f,
        scope = scope,
        sharingStarted = SharingStarted.Eagerly
    )

    override val fontSize = dataStoreStateFlow(
        key = intPreferencesKey("fontSize"),
        default = FontSize.Small,
        scope = scope,
        sharingStarted = SharingStarted.Eagerly
    )

    // ================
    // Map
    // ================

    override val wifiPropertyEnablementMap: Map<WidgetWifiProperty, StateFlow<Boolean>> by lazy {
        getStateFlowMap(WidgetWifiProperty.entries.associateBy { it.isEnabledDSE })
    }

    override suspend fun saveWifiPropertyEnablementMap(map: Map<WidgetWifiProperty, Boolean>) {
        saveMap(map.mapKeys { (k, _) -> k.isEnabledDSE })
    }

    override val ipSubPropertyEnablementMap: Map<WidgetWifiProperty.IP.SubProperty, StateFlow<Boolean>> by lazy {
        getStateFlowMap(
            WidgetWifiProperty.IP.entries
                .flatMap { it.subProperties }
                .associateBy { it.isEnabledDse }
        )
    }

    override suspend fun saveIPSubPropertyEnablementMap(map: Map<WidgetWifiProperty.IP.SubProperty, Boolean>) {
        saveMap(map.mapKeys { (k, _) -> k.isEnabledDse })
    }

    override val refreshingParametersEnablementMap: Map<WidgetRefreshingParameter, StateFlow<Boolean>> by lazy {
        getStateFlowMap(WidgetRefreshingParameter.entries.associateBy { it.isEnabledDSE })
    }

    override suspend fun saveRefreshingParametersEnablementMap(map: Map<WidgetRefreshingParameter, Boolean>) {
        saveMap(map.mapKeys { (k, _) -> k.isEnabledDSE })
    }

    override val bottomRowElementEnablementMap: Map<WidgetBottomRowElement, StateFlow<Boolean>> by lazy {
        getStateFlowMap(WidgetBottomRowElement.entries.associateBy { it.isEnabledDSE })
    }

    override suspend fun saveBottomRowElementEnablementMap(map: Map<WidgetBottomRowElement, Boolean>) {
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

private val WidgetWifiProperty.isEnabledDSE
    get() = DataStoreEntry.UniType.Impl(
        preferencesKey = booleanPreferencesKey(preferencesKeyName),
        defaultValue = defaultIsEnabled,
    )

private val WidgetWifiProperty.IP.SubProperty.isEnabledDse
    get() =
        DataStoreEntry.UniType.Impl(
            preferencesKey = booleanPreferencesKey("${property.preferencesKeyName}.${kind.preferencesKeyName}"),
            defaultValue = true,
        )

private val WidgetBottomRowElement.isEnabledDSE
    get() = DataStoreEntry.UniType.Impl(
        preferencesKey = booleanPreferencesKey(
            when (this) {
                WidgetBottomRowElement.LastRefreshTimeDisplay -> "ShowDateTime"
                WidgetBottomRowElement.RefreshButton -> "WidgetButton.Refresh"
                WidgetBottomRowElement.GoToWidgetSettingsButton -> "WidgetButton.GoToWidgetSettings"
                WidgetBottomRowElement.GoToWifiSettingsButton -> "WidgetButton.GoToWifiSettings"
            }
        ),
        defaultValue = true,
    )

private val WidgetRefreshingParameter.isEnabledDSE
    get() = DataStoreEntry.UniType.Impl(
        preferencesKey = booleanPreferencesKey(
            when (this) {
                WidgetRefreshingParameter.RefreshOnLowBattery -> "RefreshOnBatteryLow"
                WidgetRefreshingParameter.RefreshPeriodically -> "RefreshPeriodically"
            }
        ),
        defaultValue = defaultIsEnabled,
    )

private val Any.preferencesKeyName: String
    get() = this::class.simpleName!!