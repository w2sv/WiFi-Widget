package com.w2sv.domain.repository

import com.w2sv.androidutils.datastorage.preferences_datastore.flow.DataStoreFlow
import com.w2sv.domain.model.Theme

interface PreferencesRepository {
    val inAppTheme: DataStoreFlow<Theme>
    val useDynamicTheme: DataStoreFlow<Boolean>
    val useAmoledBlackTheme: DataStoreFlow<Boolean>
}