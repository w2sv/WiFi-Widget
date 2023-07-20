package com.w2sv.common.data.repositories

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import com.w2sv.androidutils.datastorage.datastore.preferences.PreferencesDataStoreRepository
import com.w2sv.common.enums.Theme
import com.w2sv.common.enums.WidgetColor
import com.w2sv.common.enums.WidgetRefreshingParameter
import com.w2sv.common.enums.WifiProperty
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class WidgetConfigurationRepository @Inject constructor(
    dataStore: DataStore<Preferences>
) : PreferencesDataStoreRepository(dataStore) {

    val theme: Flow<Theme> = getEnumFlow(Key.WIDGET_THEME, Theme.DeviceDefault)

    val opacity: Flow<Float> = getFlow(Key.OPACITY, 1.0f)

    val refreshingParameters = getFlowMap(WidgetRefreshingParameter.values().toList())

    val customColors = getFlowMap(WidgetColor.values().toList())

    val wifiProperties = getFlowMap(WifiProperty.values().toList())

    object Key {
        val OPACITY = floatPreferencesKey("opacity")
        val WIDGET_THEME = intPreferencesKey("widgetTheme")
    }
}