package com.w2sv.datastore.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import com.w2sv.androidutils.os.dynamicColorsSupported
import com.w2sv.domain.model.Theme
import com.w2sv.domain.model.ThemeSettings
import com.w2sv.domain.repository.PreferencesRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.map

@Singleton
internal class PreferencesRepositoryImpl @Inject constructor(private val dataStore: DataStore<Preferences>) : PreferencesRepository {
    private val themeKey = intPreferencesKey("inAppTheme")
    private val dynamicColorsKey = booleanPreferencesKey("useDynamicTheme")
    private val amoledBlackKey = booleanPreferencesKey("useAmoledBlackTheme")

    override val themeSettings = dataStore.data.map { it.toThemeSettings() }

    override suspend fun updateThemeSettings(transform: (ThemeSettings) -> ThemeSettings) {
        dataStore.edit { preferences ->
            val settings = transform(preferences.toThemeSettings())
            preferences[themeKey] = settings.theme.ordinal
            preferences[amoledBlackKey] = settings.useAmoledBlackTheme
            preferences[dynamicColorsKey] = settings.useDynamicColors
        }
    }

    private fun Preferences.toThemeSettings() =
        ThemeSettings(
            theme = this[themeKey]?.let(Theme.entries::getOrNull) ?: Theme.Default,
            useAmoledBlackTheme = this[amoledBlackKey] ?: false,
            useDynamicColors = this[dynamicColorsKey] ?: dynamicColorsSupported
        )
}
