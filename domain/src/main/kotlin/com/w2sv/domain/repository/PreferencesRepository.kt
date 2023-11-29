package com.w2sv.domain.repository

import com.w2sv.androidutils.datastorage.datastore.preferences.PersistedValue
import com.w2sv.domain.model.Theme

interface PreferencesRepository {
    val locationAccessPermissionRationalShown: PersistedValue.UniTyped<Boolean>
    val locationAccessPermissionRequested: PersistedValue.UniTyped<Boolean>
    val inAppTheme: PersistedValue.EnumValued<Theme>
    val useDynamicTheme: PersistedValue.UniTyped<Boolean>
}