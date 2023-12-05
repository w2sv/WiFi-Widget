package com.w2sv.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import com.w2sv.androidutils.datastorage.datastore.preferences.PreferencesDataStoreRepository
import com.w2sv.common.utils.dynamicColorsSupported
import com.w2sv.domain.model.Theme
import com.w2sv.domain.repository.PreferencesRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreferencesRepositoryImpl @Inject constructor(
    dataStore: DataStore<Preferences>,
) : PreferencesDataStoreRepository(dataStore),
    PreferencesRepository {

    override val locationAccessPermissionRationalShown =
        getPersistedValue(booleanPreferencesKey("locationPermissionDialogAnswered"), false)

    override val locationAccessPermissionRequested = getPersistedValue(
        booleanPreferencesKey("locationAccessPermissionRequestedAtLeastOnce"),
        false
    )

    override val inAppTheme =
        getPersistedValue(intPreferencesKey("inAppTheme"), Theme.SystemDefault)

    override val useDynamicTheme =
        getPersistedValue(booleanPreferencesKey("useDynamicTheme"), dynamicColorsSupported)
}
