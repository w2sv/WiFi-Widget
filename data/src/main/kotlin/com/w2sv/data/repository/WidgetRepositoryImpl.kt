package com.w2sv.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import com.w2sv.androidutils.datastorage.datastore.DataStoreEntry
import com.w2sv.androidutils.datastorage.datastore.DataStoreRepository
import com.w2sv.data.model.isEnabledDSE
import com.w2sv.data.model.isEnabledDse
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
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
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

    override val coloring = dataStoreStateFlow(
        key = intPreferencesKey("widgetColoring"),
        default = WidgetColoring.Preset,
        scope = scope,
        sharingStarted = SharingStarted.Eagerly
    )

    private val theme = dataStoreFlow(
        key = intPreferencesKey("widgetTheme"),
        default = WidgetColoring.Data.Preset.Defaults.THEME,
    )

    private val useDynamicColors = dataStoreFlow(
        key = booleanPreferencesKey("widgetConfiguration.useDynamicColor"),
        default = WidgetColoring.Data.Preset.Defaults.USE_DYNAMIC_COLORS
    )

    override val presetColoringData by lazy {
        combine(theme, useDynamicColors) { a, b ->
            WidgetColoring.Data.Preset(theme = a, useDynamicColors = b)
        }
            .stateIn(
                scope = scope,
                started = SharingStarted.Eagerly,
                initialValue = WidgetColoring.Data.Preset()
            )
    }

    override suspend fun savePresetColoringData(data: WidgetColoring.Data.Preset) {
        theme.save(data.theme)
        useDynamicColors.save(data.useDynamicColors)
    }

    private val backgroundColor = dataStoreFlow(
        key = intPreferencesKey("Background"),
        default = WidgetColoring.Data.Custom.Defaults.BACKGROUND,
    )

    private val primaryColor = dataStoreFlow(
        key = intPreferencesKey("Labels"),
        default = WidgetColoring.Data.Custom.Defaults.PRIMARY,
    )

    private val secondaryColor = dataStoreFlow(
        key = intPreferencesKey("Other"),
        default = WidgetColoring.Data.Custom.Defaults.SECONDARY,
    )

    override val customColoringData: StateFlow<WidgetColoring.Data.Custom> by lazy {
        combine(backgroundColor, primaryColor, secondaryColor) { (a, b, c) ->
            WidgetColoring.Data.Custom(background = a, primary = b, secondary = c)
        }
            .stateIn(
                scope = scope,
                started = SharingStarted.Eagerly,
                initialValue = WidgetColoring.Data.Custom()
            )
    }

    override suspend fun saveCustomColoringData(data: WidgetColoring.Data.Custom) {
        backgroundColor.save(data.background)
        primaryColor.save(data.primary)
        secondaryColor.save(data.secondary)
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
