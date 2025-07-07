package com.w2sv.datastore.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import com.w2sv.datastoreutils.preferences.PreferencesDataStoreRepository
import com.w2sv.domain.repository.PermissionRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class PermissionRepositoryImpl @Inject constructor(dataStore: DataStore<Preferences>) :
    PreferencesDataStoreRepository(dataStore),
    PermissionRepository {

    override val locationAccessPermissionRationalShown =
        dataStoreFlow(booleanPreferencesKey("locationPermissionDialogAnswered")) { false }

    override val locationAccessPermissionRequested =
        dataStoreFlow(booleanPreferencesKey("locationAccessPermissionRequestedAtLeastOnce")) { false }
}
