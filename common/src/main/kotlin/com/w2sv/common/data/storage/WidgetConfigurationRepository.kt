package com.w2sv.common.data.storage

import androidx.annotation.FloatRange
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import com.w2sv.androidutils.coroutines.getSynchronousMap
import com.w2sv.androidutils.datastorage.datastore.preferences.PreferencesDataStoreRepository
import com.w2sv.common.data.model.Theme
import com.w2sv.common.data.model.WidgetAppearance
import com.w2sv.common.data.model.WidgetColorSection
import com.w2sv.common.data.model.WidgetColors
import com.w2sv.common.data.model.WidgetRefreshing
import com.w2sv.common.data.model.WidgetRefreshingParameter
import com.w2sv.common.data.model.WidgetTheme
import com.w2sv.common.data.model.WifiProperty
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

class WidgetConfigurationRepository @Inject constructor(
    dataStore: DataStore<Preferences>
) : PreferencesDataStoreRepository(dataStore) {

    val customColorsMap = mapOf(
        WidgetColorSection.Background to getFlow(Key.LABELS, -9430904),  // Purplish
        WidgetColorSection.Labels to getFlow(Key.LABELS, -65536),  // Red
        WidgetColorSection.Other to getFlow(Key.OTHER, -1),  // White
    )

    val customColors: Flow<WidgetColors> = combine(
        customColorsMap.getValue(WidgetColorSection.Background),
        customColorsMap.getValue(WidgetColorSection.Labels),
        customColorsMap.getValue(WidgetColorSection.Other),
        transform = { background, labels, other ->
            WidgetColors(background, labels, other)
        }
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

    val refreshing: Flow<WidgetRefreshing> = combine(
        refreshingParametersMap.getValue(WidgetRefreshingParameter.RefreshPeriodically),
        refreshingParametersMap.getValue(WidgetRefreshingParameter.RefreshOnLowBattery),
        transform = { refreshPeriodically, refreshOnLowBattery ->
            WidgetRefreshing(
                refreshPeriodically = refreshPeriodically,
                refreshOnLowBattery = refreshOnLowBattery
            )
        }
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

    val appearance: Flow<WidgetAppearance> = combine(
        theme,
        customColors,
        opacity,
        refreshingParametersMap.getValue(WidgetRefreshingParameter.DisplayLastRefreshDateTime),
        transform = { theme, customColors, opacity, displayLastRefreshDateTime ->
            WidgetAppearance(
                theme = when (theme) {
                    Theme.Light -> WidgetTheme.Light
                    Theme.DeviceDefault -> WidgetTheme.DeviceDefault
                    Theme.Dark -> WidgetTheme.Dark
                    Theme.Custom -> WidgetTheme.Custom(
                        customColors
                    )
                },
                opacity = opacity,
                displayLastRefreshDateTime = displayLastRefreshDateTime
            )
        }
    )

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