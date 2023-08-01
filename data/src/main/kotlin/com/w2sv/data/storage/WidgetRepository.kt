package com.w2sv.data.storage

import androidx.annotation.FloatRange
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import com.w2sv.androidutils.coroutines.getSynchronousMap
import com.w2sv.androidutils.datastorage.datastore.preferences.PreferencesDataStoreRepository
import com.w2sv.data.model.Theme
import com.w2sv.data.model.WidgetColorSection
import com.w2sv.data.model.WidgetRefreshingParameter
import com.w2sv.data.model.WifiProperty
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class WidgetRepository @Inject constructor(
    dataStore: DataStore<Preferences>
) : PreferencesDataStoreRepository(dataStore) {

    val customColorsMap = mapOf(
        WidgetColorSection.Background to getFlow(Key.LABELS, -9430904),  // Purplish
        WidgetColorSection.Labels to getFlow(Key.LABELS, -65536),  // Red
        WidgetColorSection.Other to getFlow(Key.OTHER, -1),  // White
    )

    suspend fun saveCustomColors(colorMap: Map<WidgetColorSection, Int>) {
        dataStore.edit {
            it[Key.BACKGROUND] = colorMap.getValue(WidgetColorSection.Background)
            it[Key.LABELS] = colorMap.getValue(WidgetColorSection.Labels)
            it[Key.OTHER] = colorMap.getValue(WidgetColorSection.Other)
        }
    }

    val theme: Flow<Theme> = getEnumFlow(Key.WIDGET_THEME, Theme.DeviceDefault)

    suspend fun saveTheme(theme: Theme) {
        save(Key.WIDGET_THEME, theme)
    }

    val opacity: Flow<Float> = getFlow(Key.OPACITY, 1.0f)

    suspend fun saveOpacity(@FloatRange(0.0, 1.0) opacity: Float) {
        save(Key.OPACITY, opacity)
    }

    val refreshingParametersMap: Map<WidgetRefreshingParameter, Flow<Boolean>> =
        mapOf(
            WidgetRefreshingParameter.RefreshPeriodically to getFlow(
                Key.REFRESH_PERIODICALLY,
                true
            ),
            WidgetRefreshingParameter.RefreshOnLowBattery to getFlow(
                Key.REFRESH_ON_LOW_BATTERY,
                false
            ),
            WidgetRefreshingParameter.DisplayLastRefreshDateTime to getFlow(
                Key.DISPLAY_LAST_REFRESH_DATE_TIME,
                true
            ),
        )

    suspend fun saveRefreshingParameters(parameters: Map<WidgetRefreshingParameter, Boolean>) {
        dataStore.edit {
            it[Key.REFRESH_PERIODICALLY] =
                parameters.getValue(WidgetRefreshingParameter.RefreshPeriodically)
            it[Key.REFRESH_ON_LOW_BATTERY] =
                parameters.getValue(WidgetRefreshingParameter.RefreshOnLowBattery)
            it[Key.DISPLAY_LAST_REFRESH_DATE_TIME] =
                parameters.getValue(WidgetRefreshingParameter.DisplayLastRefreshDateTime)
        }
    }

    val wifiProperties = getFlowMap(WifiProperty.values().toList())

    fun getSetWifiProperties(): Set<WifiProperty> =
        wifiProperties.getSynchronousMap().filterValues { it }.keys

    object Key {
        val OPACITY = floatPreferencesKey("opacity")
        val WIDGET_THEME = intPreferencesKey("widgetTheme")
        val DISPLAY_LAST_REFRESH_DATE_TIME = booleanPreferencesKey("ShowDateTime")

        val REFRESH_PERIODICALLY = booleanPreferencesKey("RefreshPeriodically")
        val REFRESH_ON_LOW_BATTERY = booleanPreferencesKey("RefreshOnLowBattery")

        val BACKGROUND = intPreferencesKey("Background")
        val LABELS = intPreferencesKey("Labels")
        val OTHER = intPreferencesKey("Other")
    }
}