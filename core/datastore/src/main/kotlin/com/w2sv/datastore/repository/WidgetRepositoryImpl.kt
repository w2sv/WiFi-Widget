package com.w2sv.datastore.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.w2sv.common.utils.enabledKeysFlow
import com.w2sv.datastore.proto.widgetcoloring.WidgetColoringDataSource
import com.w2sv.datastoreutils.datastoreflow.DataStoreFlow
import com.w2sv.datastoreutils.preferences.PreferencesDataStoreRepository
import com.w2sv.datastoreutils.preferences.map.DataStoreEntry
import com.w2sv.datastoreutils.preferences.map.DataStoreFlowMap
import com.w2sv.domain.model.FontSize
import com.w2sv.domain.model.LocationParameter
import com.w2sv.domain.model.PropertyValueAlignment
import com.w2sv.domain.model.WidgetBottomBarElement
import com.w2sv.domain.model.WidgetColoring
import com.w2sv.domain.model.WidgetRefreshingParameter
import com.w2sv.domain.model.WifiProperty
import com.w2sv.domain.repository.WidgetRepository
import com.w2sv.kotlinutils.coroutines.flow.stateInWithBlockingInitial
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

@Singleton
internal class WidgetRepositoryImpl @Inject constructor(
    dataStore: DataStore<Preferences>,
    private val widgetColoringDataSource: WidgetColoringDataSource
) : PreferencesDataStoreRepository(dataStore),
    WidgetRepository {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override val coloringConfig: Flow<WidgetColoring.Config> = widgetColoringDataSource.config

    override suspend fun saveColoringConfig(config: WidgetColoring.Config) {
        widgetColoringDataSource.saveConfig(config)
    }

    override val opacity = dataStoreFlow(
        key = floatPreferencesKey("opacity"),
        default = { 1.0f }
    )

    override val fontSize = enumDataStoreFlow(
        key = intPreferencesKey("fontSize"),
        default = { FontSize.Small }
    )

    override val propertyValueAlignment = enumDataStoreFlow(
        key = intPreferencesKey("propertyValueAlignment"),
        default = { PropertyValueAlignment.Right }
    )

    // ================
    // Maps
    // ================

    override val wifiPropertyEnablementMap: DataStoreFlowMap<WifiProperty, Boolean> =
        dataStoreFlowMap(WifiProperty.entries.associateWith { it.isEnabledDSE })

    override val wifiPropertyOrder: DataStoreFlow<List<WifiProperty>> = listDataStoreFlow(
        key = stringPreferencesKey("wifiPropertyOrder"),
        default = { WifiProperty.entries },
        serialize = { it.joinToString(separator = ",") { property -> property.ordinal.toString() } },
        deserialize = { it.split(",").map { ordinalString -> WifiProperty.entries[ordinalString.toInt()] } }
    )

    override val sortedEnabledWifiProperties: StateFlow<List<WifiProperty>> by lazy {
        combine(
            wifiPropertyEnablementMap.enabledKeysFlow(),
            wifiPropertyOrder
        ) { enabledKeys, order ->
            enabledKeys.sortedBy { order.indexOf(it) }
        }
            .stateInWithBlockingInitial(scope)
    }

    override val ipSubPropertyEnablementMap: DataStoreFlowMap<WifiProperty.IP.SubProperty, Boolean> =
        dataStoreFlowMap(
            WifiProperty.IP.entries
                .flatMap { it.subProperties }
                .associateWith { it.isEnabledDse }
        )

    override val enabledIpSubProperties: StateFlow<Set<WifiProperty.IP.SubProperty>> by lazy {
        ipSubPropertyEnablementMap.enabledKeysFlow().stateInWithBlockingInitial(scope)
    }

    override val bottomRowElementEnablementMap: DataStoreFlowMap<WidgetBottomBarElement, Boolean> =
        dataStoreFlowMap(WidgetBottomBarElement.entries.associateWith { it.isEnabledDSE })

    override val refreshingParametersEnablementMap: DataStoreFlowMap<WidgetRefreshingParameter, Boolean> =
        dataStoreFlowMap(WidgetRefreshingParameter.entries.associateWith { it.isEnabledDSE })

    override val refreshInterval: DataStoreFlow<Duration> =
        DataStoreFlow(
            flow = getFlow(
                preferencesKey = intPreferencesKey("refreshInterval"),
                defaultValue = { 15 }
            )
                .map { it.minutes },
            default = { 15.minutes },
            save = { save(intPreferencesKey("refreshInterval"), it.inWholeMinutes.toInt()) }
        )

    override val locationParameters: DataStoreFlowMap<LocationParameter, Boolean> =
        dataStoreFlowMap(LocationParameter.entries.associateWith { it.isEnabledDSE })

    override val enabledLocationParameters: StateFlow<Set<LocationParameter>> by lazy {
        locationParameters.enabledKeysFlow().stateInWithBlockingInitial(scope)
    }
}

private val WifiProperty.isEnabledDSE
    get() = DataStoreEntry.UniType.Impl(
        preferencesKey = booleanPreferencesKey(preferencesKeyName),
        defaultValue = { defaultIsEnabled }
    )

private val WifiProperty.IP.SubProperty.isEnabledDse
    get() =
        DataStoreEntry.UniType.Impl(
            preferencesKey = booleanPreferencesKey("${property.preferencesKeyName}.${kind.preferencesKeyName}"),
            defaultValue = { true }
        )

private val WidgetBottomBarElement.isEnabledDSE
    get() = DataStoreEntry.UniType.Impl(
        preferencesKey = booleanPreferencesKey(
            when (this) {
                WidgetBottomBarElement.LastRefreshTimeDisplay -> "ShowDateTime"
                WidgetBottomBarElement.RefreshButton -> "WidgetButton.Refresh"
                WidgetBottomBarElement.GoToWidgetSettingsButton -> "WidgetButton.GoToWidgetSettings"
                WidgetBottomBarElement.GoToWifiSettingsButton -> "WidgetButton.GoToWifiSettings"
            }
        ),
        defaultValue = { true }
    )

private val WidgetRefreshingParameter.isEnabledDSE
    get() = DataStoreEntry.UniType.Impl(
        preferencesKey = booleanPreferencesKey(
            when (this) {
                WidgetRefreshingParameter.RefreshOnLowBattery -> "RefreshOnBatteryLow"
                WidgetRefreshingParameter.RefreshPeriodically -> "RefreshPeriodically"
            }
        ),
        defaultValue = { defaultIsEnabled }
    )

private val LocationParameter.isEnabledDSE
    get() = DataStoreEntry.UniType.Impl(
        preferencesKey = booleanPreferencesKey(name),
        defaultValue = {
            when (this) {
                LocationParameter.ZipCode -> true
                LocationParameter.District -> false
                LocationParameter.City -> true
                LocationParameter.Region -> false
                LocationParameter.Country -> true
                LocationParameter.Continent -> false
            }
        }
    )

private val Any.preferencesKeyName: String
    get() = this::class.simpleName!!
