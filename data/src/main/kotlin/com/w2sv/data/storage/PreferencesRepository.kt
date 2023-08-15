package com.w2sv.data.storage

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import com.w2sv.androidutils.datastorage.datastore.preferences.PreferencesDataStoreRepository
import com.w2sv.data.model.Theme
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreferencesRepository @Inject constructor(
    dataStore: DataStore<Preferences>
) : PreferencesDataStoreRepository(dataStore) {

    val locationAccessPermissionRationalShown: Flow<Boolean> =
        getFlow(Key.LOCATION_ACCESS_PERMISSION_RATIONAL_SHOWN, false)

    suspend fun saveLocationAccessPermissionRationalShown(value: Boolean) {
        save(Key.LOCATION_ACCESS_PERMISSION_RATIONAL_SHOWN, value)
    }

    val locationAccessPermissionRequestedAtLeastOnce: Flow<Boolean> =
        getFlow(Key.LOCATION_ACCESS_PERMISSION_REQUESTED_AT_LEAST_ONCE, false)

    suspend fun saveLocationAccessPermissionRequestedAtLeastOnce(value: Boolean) {
        save(Key.LOCATION_ACCESS_PERMISSION_REQUESTED_AT_LEAST_ONCE, value)
    }

    val inAppTheme: Flow<Theme> = getEnumFlow(Key.IN_APP_THEME, Theme.SystemDefault)

    val useDynamicTheme: Flow<Boolean> = getFlow(Key.USE_DYNAMIC_THEME, false)

    suspend fun saveUseDynamicTheme(value: Boolean) {
        save(Key.USE_DYNAMIC_THEME, value)
    }

    suspend fun saveInAppTheme(theme: Theme) {
        save(Key.IN_APP_THEME, theme)
    }

    private object Key {
        val LOCATION_ACCESS_PERMISSION_RATIONAL_SHOWN =
            booleanPreferencesKey("locationPermissionDialogAnswered")
        val LOCATION_ACCESS_PERMISSION_REQUESTED_AT_LEAST_ONCE =
            booleanPreferencesKey("locationAccessPermissionRequestedAtLeastOnce")
        val IN_APP_THEME = intPreferencesKey("inAppTheme")
        val USE_DYNAMIC_THEME = booleanPreferencesKey("useDynamicTheme")
    }
}