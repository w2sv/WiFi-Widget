package com.w2sv.data.model.widget

import androidx.datastore.preferences.core.intPreferencesKey
import com.w2sv.androidutils.datastorage.datastore.preferences.DataStoreEntry

enum class WidgetColor(
    dataStoreEntry: DataStoreEntry.UniType<Int>,
) : DataStoreEntry.UniType<Int> by dataStoreEntry {

    Background(DataStoreEntry.UniType.Impl(intPreferencesKey("Background"), -7859146)),
    Primary(DataStoreEntry.UniType.Impl(intPreferencesKey("Labels"), -5898336)),
    Secondary(DataStoreEntry.UniType.Impl(intPreferencesKey("Other"), -1)),
}
