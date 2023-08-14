package com.w2sv.data.model.widget

import androidx.datastore.preferences.core.booleanPreferencesKey
import com.w2sv.androidutils.datastorage.datastore.preferences.DataStoreEntry

enum class WidgetRefreshingParameter(
    isEnabledDataStoreEntry: DataStoreEntry.UniType<Boolean>
) : DataStoreEntry.UniType<Boolean> by isEnabledDataStoreEntry {

    RefreshPeriodically(
        DataStoreEntry.UniType.Impl(
            booleanPreferencesKey("RefreshPeriodically"),
            true
        )
    ),
    RefreshOnLowBattery(
        DataStoreEntry.UniType.Impl(
            booleanPreferencesKey("RefreshOnBatteryLow"),
            false
        )
    ),
    DisplayLastRefreshDateTime(
        DataStoreEntry.UniType.Impl(
            booleanPreferencesKey("ShowDateTime"),
            true
        )
    )
}