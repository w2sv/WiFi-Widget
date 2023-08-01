package com.w2sv.common.data.storage

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import com.w2sv.androidutils.datastorage.datastore.preferences.PreferencesDataStoreRepository
import com.w2sv.common.data.model.Theme
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class PreferencesRepository @Inject constructor(
    dataStore: DataStore<Preferences>
) : PreferencesDataStoreRepository(dataStore) {

    val locationAccessPermissionRationalShown: Flow<Boolean> =
        getFlow(Key.LOCATION_ACCESS_PERMISSION_RATIONAL_SHOWN, false)

    val locationAccessPermissionRequestedAtLeastOnce: Flow<Boolean> =
        getFlow(Key.LOCATION_ACCESS_PERMISSION_REQUESTED_AT_LEAST_ONCE, false)

    val inAppTheme: Flow<Theme> = getEnumFlow(Key.IN_APP_THEME, Theme.DeviceDefault)

    suspend fun saveInAppTheme(theme: Theme) {
        save(Key.IN_APP_THEME, theme)
    }

    object Key {
        val LOCATION_ACCESS_PERMISSION_RATIONAL_SHOWN =
            booleanPreferencesKey("locationPermissionDialogAnswered")
        val LOCATION_ACCESS_PERMISSION_REQUESTED_AT_LEAST_ONCE =
            booleanPreferencesKey("locationAccessPermissionRequestedAtLeastOnce")
        val IN_APP_THEME = intPreferencesKey("inAppTheme")
    }
}