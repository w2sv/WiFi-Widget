package com.w2sv.common

import androidx.annotation.StringRes
import androidx.datastore.preferences.core.booleanPreferencesKey
import com.w2sv.common.datastore.DataStoreProperty

enum class WidgetRefreshingParameter(
    override val defaultValue: Boolean,
    @StringRes val labelRes: Int
) :
    DataStoreProperty<Boolean> {

    RefreshPeriodically(true, R.string.refresh_periodically),
    RefreshOnBatteryLow(false, R.string.refresh_on_low_battery);

    override val preferencesKey = booleanPreferencesKey(name)
}