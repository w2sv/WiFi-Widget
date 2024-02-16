package com.w2sv.data.model

import androidx.datastore.preferences.core.booleanPreferencesKey
import com.w2sv.androidutils.datastorage.datastore.DataStoreEntry
import com.w2sv.domain.model.WidgetBottomRowElement
import com.w2sv.domain.model.WidgetRefreshingParameter
import com.w2sv.domain.model.WidgetWifiProperty

private val Any.preferencesKeyName: String
    get() = this::class.simpleName!!

internal val WidgetWifiProperty.isEnabledDSE
    get() = DataStoreEntry.UniType.Impl(
        preferencesKey = booleanPreferencesKey(preferencesKeyName),
        defaultValue = defaultIsEnabled,
    )

internal val WidgetWifiProperty.IP.SubProperty.isEnabledDse
    get() =
        DataStoreEntry.UniType.Impl(
            preferencesKey = booleanPreferencesKey("${property.preferencesKeyName}.${kind.preferencesKeyName}"),
            defaultValue = true,
        )

internal val WidgetBottomRowElement.isEnabledDSE
    get() = DataStoreEntry.UniType.Impl(
        preferencesKey = booleanPreferencesKey(
            when (this) {
                WidgetBottomRowElement.LastRefreshTimeDisplay -> "ShowDateTime"
                WidgetBottomRowElement.RefreshButton -> "WidgetButton.Refresh"
                WidgetBottomRowElement.GoToWidgetSettingsButton -> "WidgetButton.GoToWidgetSettings"
                WidgetBottomRowElement.GoToWifiSettingsButton -> "WidgetButton.GoToWifiSettings"
            }
        ),
        defaultValue = true,
    )

internal val WidgetRefreshingParameter.isEnabledDSE
    get() = DataStoreEntry.UniType.Impl(
        preferencesKey = booleanPreferencesKey(
            when (this) {
                WidgetRefreshingParameter.RefreshOnLowBattery -> "RefreshOnBatteryLow"
                WidgetRefreshingParameter.RefreshPeriodically -> "RefreshPeriodically"
            }
        ),
        defaultValue = defaultIsEnabled,
    )