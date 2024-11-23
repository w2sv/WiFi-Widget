package com.w2sv.datastore.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import com.w2sv.androidutils.os.dynamicColorsSupported
import com.w2sv.datastoreutils.datastoreflow.DataStoreFlow
import com.w2sv.datastoreutils.preferences.PreferencesDataStoreRepository
import com.w2sv.domain.model.Theme
import com.w2sv.domain.repository.PreferencesRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class PreferencesRepositoryImpl @Inject constructor(
    dataStore: DataStore<Preferences>
) : PreferencesDataStoreRepository(dataStore),
    PreferencesRepository {

    override val inAppTheme =
        enumDataStoreFlow(intPreferencesKey("inAppTheme")) { Theme.Default }

    override val useDynamicTheme =
        dataStoreFlow(booleanPreferencesKey("useDynamicTheme")) { dynamicColorsSupported }

    override val useAmoledBlackTheme =
        dataStoreFlow(booleanPreferencesKey("useAmoledBlackTheme")) { false }

    override val propertyReorderingDiscoveryShown: DataStoreFlow<Boolean> =
        dataStoreFlow(booleanPreferencesKey("propertyReorderingDiscoveryShown")) { false }
}
