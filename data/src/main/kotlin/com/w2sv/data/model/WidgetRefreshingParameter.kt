package com.w2sv.data.model

import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import com.w2sv.androidutils.datastorage.datastore.preferences.DataStoreEntry

enum class WidgetRefreshingParameter(
    override val preferencesKey: Preferences.Key<Boolean>,
    override val defaultValue: Boolean
) : DataStoreEntry.UniType<Boolean> {

    RefreshPeriodically(booleanPreferencesKey("RefreshPeriodically"), true),
    RefreshOnLowBattery(booleanPreferencesKey("RefreshOnBatteryLow"), false),
    DisplayLastRefreshDateTime(booleanPreferencesKey("ShowDateTime"), true)
}