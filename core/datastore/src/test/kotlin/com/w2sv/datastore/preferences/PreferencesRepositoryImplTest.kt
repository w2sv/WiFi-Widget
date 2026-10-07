package com.w2sv.datastore.preferences

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import com.w2sv.domain.model.Theme
import com.w2sv.domain.model.ThemeSettings
import java.io.File
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
internal class PreferencesRepositoryImplTest {

    @get:Rule
    val temporaryFolder = TemporaryFolder()

    @Test
    fun `consecutive theme updates preserve persisted values from before each update`() =
        runTest {
            val file = File(temporaryFolder.root, "theme.preferences_pb")
            val dataStore = PreferenceDataStoreFactory.create(
                scope = backgroundScope,
                produceFile = { file }
            )
            val themeKey = intPreferencesKey("inAppTheme")
            val amoledBlackKey = booleanPreferencesKey("useAmoledBlackTheme")
            val dynamicColorsKey = booleanPreferencesKey("useDynamicTheme")
            val unrelatedKey = booleanPreferencesKey("unrelatedPreference")

            dataStore.edit {
                it[themeKey] = Theme.Light.ordinal
                it[amoledBlackKey] = true
                it[dynamicColorsKey] = false
                it[unrelatedKey] = true
            }

            val repository = PreferencesRepositoryImpl(dataStore)
            repository.updateThemeSettings { it.copy(theme = Theme.Dark) }
            repository.updateThemeSettings { it.copy(useDynamicColors = true) }

            assertEquals(ThemeSettings(Theme.Dark, useAmoledBlackTheme = true, useDynamicColors = true), repository.themeSettings.first())
            assertTrue(dataStore.data.first()[unrelatedKey] == true)
        }
}
