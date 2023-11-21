package com.w2sv.data.repositories

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import com.w2sv.androidutils.coroutines.getSynchronousMap
import com.w2sv.androidutils.datastorage.datastore.preferences.DataStoreEntry
import com.w2sv.androidutils.datastorage.datastore.preferences.PreferencesDataStoreRepository
import com.w2sv.data.model.isEnabledDSE
import com.w2sv.data.model.valueDSE
import com.w2sv.domain.model.Theme
import com.w2sv.domain.model.WidgetButton
import com.w2sv.domain.model.WidgetColor
import com.w2sv.domain.model.WidgetRefreshingParameter
import com.w2sv.domain.model.WidgetWifiProperty
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WidgetRepository @Inject constructor(
    dataStore: DataStore<Preferences>,
) : PreferencesDataStoreRepository(dataStore) {

    val optionsChangedWidgetId get() = _optionsChangedWidgetId.asSharedFlow()
    private val _optionsChangedWidgetId = MutableSharedFlow<Int>()

    fun onWidgetOptionsChanged(widgetId: Int) {
        CoroutineScope(Dispatchers.Default).launch {
            _optionsChangedWidgetId.emit(widgetId)
        }
    }

    fun getEnabledWifiProperties(): Set<WidgetWifiProperty> =
        getWidgetWifiPropertyEnablementMap().getSynchronousMap().filterValues { it }.keys

    // ================
    // PersistedValue
    // ================

    val theme = getPersistedValue(intPreferencesKey("widgetTheme"), Theme.SystemDefault)

    val useDynamicColors =
        getPersistedValue(booleanPreferencesKey("widgetConfiguration.useDynamicColor"), false)

    val opacity = getPersistedValue(floatPreferencesKey("opacity"), 1.0f)

    // ================
    // Map
    // ================

    fun getCustomColorsMap(): Map<WidgetColor, Flow<Int>> =
        getTypeToValueMap(WidgetColor.entries.associateBy { it.valueDSE })

    fun getWidgetWifiPropertyEnablementMap(): Map<WidgetWifiProperty, Flow<Boolean>> =
        getTypeToValueMap(WidgetWifiProperty.entries.associateBy { it.isEnabledDSE })

    fun getRefreshingParametersEnablementMap(): Map<WidgetRefreshingParameter, Flow<Boolean>> {
        return getTypeToValueMap(WidgetRefreshingParameter.entries.associateBy { it.isEnabledDSE })
    }

    fun getButtonEnablementMap(): Map<WidgetButton, Flow<Boolean>> {
        return getTypeToValueMap(WidgetButton.entries.associateBy { it.isEnabledDSE })
    }

    private fun <E, V> getTypeToValueMap(dseToType: Map<DataStoreEntry.UniType<V>, E>): Map<E, Flow<V>> =
        getFlowMap(dseToType.keys).mapKeys { (k, _) -> dseToType.getValue(k) }
}
