package com.w2sv.common.data.storage

import androidx.annotation.ColorInt
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import com.w2sv.androidutils.coroutines.getSynchronousMap
import com.w2sv.androidutils.datastorage.datastore.preferences.PreferencesDataStoreRepository
import com.w2sv.common.data.model.Theme
import com.w2sv.common.data.model.WidgetAppearance
import com.w2sv.common.data.model.WidgetColors
import com.w2sv.common.data.model.WidgetRefreshing
import com.w2sv.common.data.model.WidgetTheme
import com.w2sv.common.data.model.WifiProperty
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class WidgetConfigurationRepository @Inject constructor(
    dataStore: DataStore<Preferences>
) : PreferencesDataStoreRepository(dataStore) {

    private val widgetColors: Flow<WidgetColors> = combine(
        dataStore.data.map {
            it[intPreferencesKey("Background")] ?: Color(
                112,
                24,
                136
            )
                .toArgb()
        },
        dataStore.data.map { it[intPreferencesKey("Labels")] ?: Color.Red.toArgb() },
        dataStore.data.map { it[intPreferencesKey("Other")] ?: Color.White.toArgb() },
        transform = { background, labels, other ->
            WidgetColors(background, labels, other)
        }
    )

    val theme: Flow<WidgetTheme> = combine(
        getEnumFlow(Key.WIDGET_THEME, Theme.DeviceDefault),
        widgetColors,
        transform = { theme, widgetColors ->
            when (theme) {
                Theme.Light -> WidgetTheme.Light
                Theme.DeviceDefault -> WidgetTheme.DeviceDefault
                Theme.Dark -> WidgetTheme.Dark
                Theme.Custom -> WidgetTheme.Custom(
                    widgetColors
                )
            }
        }
    )

    val opacity: Flow<Float> = getFlow(Key.OPACITY, 1.0f)

    val displayLastRefreshDateTime: Flow<Boolean> =
        getFlow(Key.DISPLAY_LAST_REFRESH_DATE_TIME, true)

    val appearance: Flow<WidgetAppearance> = combine(
        theme,
        opacity,
        displayLastRefreshDateTime,
        transform = { theme, opacity, displayLastRefreshDateTime ->
            WidgetAppearance(
                theme = theme,
                opacity = opacity,
                displayLastRefreshDateTime = displayLastRefreshDateTime
            )
        }
    )

    val refreshing: Flow<WidgetRefreshing> = combine(
        dataStore.data.map { it[booleanPreferencesKey("RefreshPeriodically")] ?: true },
        dataStore.data.map { it[booleanPreferencesKey("RefreshOnBatteryLow")] ?: false },
        transform = { refreshPeriodically, refreshOnLowBattery ->
            WidgetRefreshing(
                refreshPeriodically = refreshPeriodically,
                refreshOnLowBattery = refreshOnLowBattery
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
    }
}

@ColorInt
private fun Int.toColor(): Color =
    Color(this)