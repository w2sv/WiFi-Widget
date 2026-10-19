package com.w2sv.domain.preferences

import kotlinx.coroutines.flow.Flow

interface PreferencesRepository {
    val themeSettings: Flow<ThemeSettings>
    suspend fun updateThemeSettings(transform: (ThemeSettings) -> ThemeSettings)
}
