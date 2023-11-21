package com.w2sv.data.repositories

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import com.w2sv.androidutils.datastorage.datastore.preferences.PreferencesDataStoreRepository
import com.w2sv.domain.model.Theme
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreferencesRepository @Inject constructor(
    dataStore: DataStore<Preferences>,
) : PreferencesDataStoreRepository(dataStore) {

    val locationAccessPermissionRationalShown =
        getPersistedValue(booleanPreferencesKey("locationPermissionDialogAnswered"), false)

    val locationAccessPermissionRequested = getPersistedValue(
        booleanPreferencesKey("locationAccessPermissionRequestedAtLeastOnce"),
        false
    )

    val inAppTheme = getPersistedValue(intPreferencesKey("inAppTheme"), Theme.SystemDefault)

    val useDynamicTheme = getPersistedValue(booleanPreferencesKey("useDynamicTheme"), false)
}
