package com.w2sv.common

import androidx.datastore.preferences.core.booleanPreferencesKey
import com.w2sv.common.preferences.DataStoreProperty

enum class WidgetRefreshingParameter(override val defaultValue: Boolean) :
    DataStoreProperty<Boolean> {
    RefreshPeriodically(true),
    RefreshOnBatteryLow(false);

    override val preferencesKey = booleanPreferencesKey(name)
}