package com.w2sv.data.storage

import androidx.annotation.FloatRange
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import com.w2sv.androidutils.coroutines.getSynchronousMap
import com.w2sv.androidutils.datastorage.datastore.preferences.PreferencesDataStoreRepository
import com.w2sv.data.model.Theme
import com.w2sv.data.model.WifiProperty
import com.w2sv.data.model.widget.WidgetButton
import com.w2sv.data.model.widget.WidgetColor
import com.w2sv.data.model.widget.WidgetRefreshingParameter
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class WidgetRepository @Inject constructor(
    dataStore: DataStore<Preferences>
) : PreferencesDataStoreRepository(dataStore) {

    val theme: Flow<Theme> = getEnumFlow(Key.WIDGET_THEME, Theme.SystemDefault)

    suspend fun saveTheme(theme: Theme) {
        save(Key.WIDGET_THEME, theme)
    }

    val useDynamicColors: Flow<Boolean> = getFlow(Key.USE_DYNAMIC_COLORS, false)

    suspend fun savUseDynamicColors(value: Boolean) {
        save(Key.USE_DYNAMIC_COLORS, value)
    }

    val customColorsMap = getFlowMap(WidgetColor.entries)

    val opacity: Flow<Float> = getFlow(Key.OPACITY, 1.0f)

    suspend fun saveOpacity(@FloatRange(0.0, 1.0) opacity: Float) {
        save(Key.OPACITY, opacity)
    }

    val wifiProperties = getFlowMap(WifiProperty.values().toList())

    val subWifiProperties =
        WifiProperty.IPProperty.values()
            .flatMap { it.subProperties }
            .associateWith { subProperty ->
                getFlow(subProperty)
            }

    fun getSetWifiProperties(): Set<WifiProperty> =
        wifiProperties.getSynchronousMap().filterValues { it }.keys

    val refreshingParametersMap = getFlowMap(WidgetRefreshingParameter.values().toList())

    val buttonMap = getFlowMap(WidgetButton.entries)

    private object Key {
        val OPACITY = floatPreferencesKey("opacity")
        val WIDGET_THEME = intPreferencesKey("widgetTheme")
        val USE_DYNAMIC_COLORS = booleanPreferencesKey("widgetConfiguration.useDynamicColor")
    }
}