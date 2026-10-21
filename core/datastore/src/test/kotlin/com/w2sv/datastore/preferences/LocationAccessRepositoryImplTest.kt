package com.w2sv.datastore.preferences

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import java.io.File
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

internal class LocationAccessRepositoryImplTest {

    @get:Rule
    val temporaryFolder = TemporaryFolder()

    @Test
    fun `permission history reads existing keys and persists each event independently`() =
        runTest {
            val dataStore = PreferenceDataStoreFactory.create(
                scope = backgroundScope,
                produceFile = { File(temporaryFolder.root, "permissions.preferences_pb") }
            )
            val rationalShownKey = booleanPreferencesKey("locationPermissionDialogAnswered")
            val requestedKey = booleanPreferencesKey("locationAccessPermissionRequestedAtLeastOnce")
            dataStore.edit { it[rationalShownKey] = true }

            val repository = LocationAccessRepositoryImpl(dataStore)
            assertTrue(repository.rationalShown.first())
            assertFalse(repository.permissionRequested.first())

            repository.recordPermissionRequested()
            assertTrue(dataStore.data.first()[requestedKey] == true)
            assertTrue(dataStore.data.first()[rationalShownKey] == true)

            dataStore.edit { it[rationalShownKey] = false }
            repository.recordRationalShown()
            assertTrue(repository.rationalShown.first())
            assertTrue(repository.permissionRequested.first())
        }
}
