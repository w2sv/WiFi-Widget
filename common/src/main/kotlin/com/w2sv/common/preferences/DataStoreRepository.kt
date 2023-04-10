package com.w2sv.common.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.w2sv.common.Theme
import com.w2sv.common.WidgetColorSection
import com.w2sv.common.WidgetRefreshingParameter
import com.w2sv.common.WifiProperty
import com.w2sv.kotlinutils.extensions.getByOrdinal
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

class DataStoreRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    val opacity: Flow<Float> = dataStore.data.map {
        it[PreferencesKey.OPACITY] ?: 1.0f
    }

    val locationPermissionDialogAnswered: Flow<Boolean> = dataStore.data.map {
        it[PreferencesKey.LOCATION_PERMISSION_DIALOG_ANSWERED] ?: false
    }

    val inAppTheme: Flow<Theme> = dataStore.data.map {
        getByOrdinal(
            it[PreferencesKey.IN_APP_THEME] ?: Theme.DeviceDefault.ordinal
        )
    }

    val widgetTheme: Flow<Theme> = dataStore.data.map {
        getByOrdinal(
            it[PreferencesKey.WIDGET_THEME] ?: Theme.DeviceDefault.ordinal
        )
    }

    val widgetRefreshingParameters = mapFromDataStoreProperties(WidgetRefreshingParameter.values())

    val customWidgetColors = mapFromDataStoreProperties(WidgetColorSection.values())

    val wifiProperties = mapFromDataStoreProperties(WifiProperty.values())

    private fun <T, P : DataStoreProperty<T>> mapFromDataStoreProperties(properties: Array<P>): Map<P, Flow<T>> =
        properties.associateWith { property ->
            dataStore.data.map {
                it[property.preferencesKey] ?: property.defaultValue
            }
        }

    fun <T, P : DataStoreProperty<T>> saveMap(map: Map<P, T>, coroutineScope: CoroutineScope) {
        coroutineScope.launch {
            dataStore.edit {
                map.forEach { (property, value) ->
                    it[property.preferencesKey] = value
                }
            }
        }
    }

    fun <T> save(value: T, preferencesKey: Preferences.Key<T>, coroutineScope: CoroutineScope) {
        coroutineScope.launch(Dispatchers.IO) {
            dataStore.edit {
                it[preferencesKey] = value
            }
        }
    }

    fun saveEnum(
        value: Enum<*>,
        preferencesKey: Preferences.Key<Int>,
        coroutineScope: CoroutineScope
    ) {
        coroutineScope.launch(Dispatchers.IO) {
            dataStore.edit {
                it[preferencesKey] = value.ordinal
            }
        }
    }
}