package com.w2sv.common.data.sources

import androidx.annotation.StringRes
import androidx.datastore.preferences.core.booleanPreferencesKey
import com.w2sv.androidutils.datastorage.datastore.preferences.DataStoreEntry
import com.w2sv.common.R

enum class WidgetRefreshingParameter(
    override val defaultValue: Boolean,
    @StringRes val labelRes: Int
) : DataStoreEntry.UniType<Boolean> {

    RefreshPeriodically(true, R.string.refresh_periodically),
    RefreshOnBatteryLow(false, R.string.refresh_on_low_battery),
    ShowDateTime(true, R.string.show_date_time);

    override val preferencesKey = booleanPreferencesKey(name)
}