package com.w2sv.data.model

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import com.w2sv.androidutils.datastorage.datastore.preferences.DataStoreEntry
import com.w2sv.domain.model.WidgetButton
import com.w2sv.domain.model.WidgetColor
import com.w2sv.domain.model.WidgetRefreshingParameter
import com.w2sv.domain.model.WidgetWifiProperty

internal val WidgetWifiProperty.isEnabledDSE
    get() = DataStoreEntry.UniType.Impl(
        preferencesKey = booleanPreferencesKey(this::class.simpleName!!),
        defaultValue = defaultIsEnabled,
    )

internal val WidgetButton.isEnabledDSE
    get() = DataStoreEntry.UniType.Impl(
        preferencesKey = booleanPreferencesKey("WidgetButton.${this::class.simpleName!!}"),
        defaultValue = true,
    )

internal val WidgetColor.valueDSE
    get() = DataStoreEntry.UniType.Impl(
        preferencesKey = intPreferencesKey(
            when (this) {
                WidgetColor.Background -> "Background"
                WidgetColor.Primary -> "Labels"
                WidgetColor.Secondary -> "Other"
            }
        ),
        defaultValue = defaultValue,
    )

internal val WidgetRefreshingParameter.isEnabledDSE
    get() = DataStoreEntry.UniType.Impl(
        preferencesKey = booleanPreferencesKey(
            when (this) {
                WidgetRefreshingParameter.DisplayLastRefreshDateTime -> "ShowDateTime"
                WidgetRefreshingParameter.RefreshOnLowBattery -> "RefreshOnBatteryLow"
                WidgetRefreshingParameter.RefreshPeriodically -> "RefreshPeriodically"
            }
        ),
        defaultValue = defaultIsEnabled,
    )