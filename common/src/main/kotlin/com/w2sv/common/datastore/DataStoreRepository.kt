package com.w2sv.common.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.w2sv.common.enums.Theme
import com.w2sv.common.enums.WidgetColorSection
import com.w2sv.common.enums.WidgetRefreshingParameter
import com.w2sv.common.enums.WifiProperty
import com.w2sv.kotlinutils.extensions.getByOrdinal
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

class DataStoreRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    val locationAccessPermissionRationalShown: Flow<Boolean> = dataStore.data.map {
        it[PreferencesKey.LOCATION_ACCESS_PERMISSION_RATIONAL_SHOWN] ?: false
    }

    val locationAccessPermissionRequestedAtLeastOnce: Flow<Boolean> = dataStore.data.map {
        it[PreferencesKey.LOCATION_ACCESS_PERMISSION_REQUESTED_AT_LEAST_ONCE] ?: false
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

    val opacity: Flow<Float> = dataStore.data.map {
        it[PreferencesKey.OPACITY] ?: 1.0f
    }

    suspend fun <T> save(preferencesKey: Preferences.Key<T>, value: T) {
        dataStore.edit {
            it[preferencesKey] = value
        }
    }

    suspend fun save(
        preferencesKey: Preferences.Key<Int>,
        enum: Enum<*>
    ) {
        dataStore.edit {
            it[preferencesKey] = enum.ordinal
        }
    }

    // ============
    // Maps
    // ============

    val widgetRefreshingParameters = mapFromDataStoreProperties(WidgetRefreshingParameter.values())

    val customWidgetColors = mapFromDataStoreProperties(WidgetColorSection.values())

    val wifiProperties = mapFromDataStoreProperties(WifiProperty.values())

    private fun <T, P : DataStoreVariable<T>> mapFromDataStoreProperties(properties: Array<P>): Map<P, Flow<T>> =
        properties.associateWith { property ->
            dataStore.data.map {
                it[property.preferencesKey] ?: property.defaultValue
            }
        }

    suspend fun <T, P : DataStoreVariable<T>> saveMap(
        map: Map<P, T>
    ) {
        dataStore.edit {
            map.forEach { (property, value) ->
                it[property.preferencesKey] = value
            }
        }
    }

    /**
     * Interface for classes interfacing with [dataStoreRepository] via a held [coroutineScope].
     */
    interface Client {

        val dataStoreRepository: DataStoreRepository
        val coroutineScope: CoroutineScope

        fun <T> saveToDataStore(key: Preferences.Key<T>, value: T) {
            coroutineScope.launch {
                dataStoreRepository.save(key, value)
            }
        }

        fun <T, P : DataStoreVariable<T>> saveMapToDataStore(
            map: Map<P, T>
        ) {
            coroutineScope.launch {
                dataStoreRepository.saveMap(map)
            }
        }
    }
}