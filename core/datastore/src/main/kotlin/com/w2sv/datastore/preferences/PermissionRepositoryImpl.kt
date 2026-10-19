package com.w2sv.datastore.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import com.w2sv.domain.permission.PermissionRepository
import com.w2sv.persistedpreferences.PersistedPreferencesAccessor
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class PermissionRepositoryImpl @Inject constructor(dataStore: DataStore<Preferences>) :
    PersistedPreferencesAccessor(dataStore),
    PermissionRepository {

    override val locationAccessPermissionRationalShown =
        persistedPreference(booleanPreferencesKey("locationPermissionDialogAnswered")) { false }

    override val locationAccessPermissionRequested =
        persistedPreference(booleanPreferencesKey("locationAccessPermissionRequestedAtLeastOnce")) { false }
}
