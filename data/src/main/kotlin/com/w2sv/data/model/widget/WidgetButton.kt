package com.w2sv.data.model.widget

import androidx.datastore.preferences.core.booleanPreferencesKey
import com.w2sv.androidutils.datastorage.datastore.preferences.DataStoreEntry

enum class WidgetButton(
    isEnabledDataStoreEntry: DataStoreEntry.UniType<Boolean>
) : DataStoreEntry.UniType<Boolean> by isEnabledDataStoreEntry {

    Refresh(DataStoreEntry.UniType.Impl(booleanPreferencesKey("WidgetButton.Refresh"), true)),
    GoToWifiSettings(
        DataStoreEntry.UniType.Impl(
            booleanPreferencesKey("WidgetButton.GoToWifiSettings"),
            true
        )
    ),
    GoToWidgetSettings(
        DataStoreEntry.UniType.Impl(
            booleanPreferencesKey("WidgetButton.GoToWidgetSettings"),
            true
        )
    )
}