package com.w2sv.domain.repository

import com.w2sv.domain.model.ThemeSettings
import kotlinx.coroutines.flow.Flow

interface PreferencesRepository {
    val themeSettings: Flow<ThemeSettings>
    suspend fun saveThemeSettings(settings: ThemeSettings)
}
