package com.w2sv.data.model.widget

import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.intPreferencesKey
import com.w2sv.androidutils.datastorage.datastore.preferences.DataStoreEntry

enum class WidgetColor(
    override val preferencesKey: Preferences.Key<Int>,
    override val defaultValue: Int
) : DataStoreEntry.UniType<Int> {
    Background(intPreferencesKey("Background"), -7859146),
    Primary(intPreferencesKey("Labels"), -5898336),
    Secondary(intPreferencesKey("Other"), -1)
}