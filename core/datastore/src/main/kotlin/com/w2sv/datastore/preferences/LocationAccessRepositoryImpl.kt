package com.w2sv.datastore.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import com.w2sv.domain.location.LocationAccessRepository
import com.w2sv.persistedpreferences.PersistedPreferencesAccessor
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class LocationAccessRepositoryImpl @Inject constructor(dataStore: DataStore<Preferences>) :
    PersistedPreferencesAccessor(dataStore),
    LocationAccessRepository {
    private val rationalShownPreference = persistedPreference(booleanPreferencesKey("locationPermissionDialogAnswered")) { false }
    private val requestedPreference = persistedPreference(booleanPreferencesKey("locationAccessPermissionRequestedAtLeastOnce")) { false }

    override val rationalShown = rationalShownPreference.flow
    override val permissionRequested = requestedPreference.flow

    override suspend fun recordRationalShown() {
        rationalShownPreference.save(true)
    }
    override suspend fun recordPermissionRequested() {
        requestedPreference.save(true)
    }
}
