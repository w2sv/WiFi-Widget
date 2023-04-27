package com.w2sv.common.enums

import androidx.annotation.StringRes
import androidx.datastore.preferences.core.booleanPreferencesKey
import com.w2sv.common.R
import com.w2sv.common.datastore.DataStoreVariable

enum class WidgetRefreshingParameter(
    override val defaultValue: Boolean,
    @StringRes val labelRes: Int
) : DataStoreVariable<Boolean> {

    RefreshPeriodically(true, R.string.refresh_periodically),
    RefreshOnBatteryLow(false, R.string.refresh_on_low_battery);

    override val preferencesKey = booleanPreferencesKey(name)
}