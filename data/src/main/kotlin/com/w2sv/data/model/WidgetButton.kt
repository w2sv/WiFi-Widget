package com.w2sv.data.model

import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import com.w2sv.androidutils.datastorage.datastore.preferences.DataStoreEntry

enum class WidgetButton(
    override val preferencesKey: Preferences.Key<Boolean>,
    override val defaultValue: Boolean
) : DataStoreEntry.UniType<Boolean> {

    Refresh(booleanPreferencesKey("WidgetButton.Refresh"), true),
    GoToWifiSettings(booleanPreferencesKey("WidgetButton.GoToWifiSettings"), true),
    GoToWidgetSettings(booleanPreferencesKey("WidgetButton.GoToWidgetSettings"), true)
}