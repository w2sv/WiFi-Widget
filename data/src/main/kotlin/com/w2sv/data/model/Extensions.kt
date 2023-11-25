package com.w2sv.data.model

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import com.w2sv.androidutils.datastorage.datastore.preferences.DataStoreEntry
import com.w2sv.domain.model.WidgetButton
import com.w2sv.domain.model.WidgetColor
import com.w2sv.domain.model.WidgetRefreshingParameter
import com.w2sv.domain.model.WidgetWifiProperty

private val Any.basePreferencesKeyName: String
    get() = this::class.simpleName!!

internal val WidgetWifiProperty.isEnabledDSE
    get() = DataStoreEntry.UniType.Impl(
        preferencesKey = booleanPreferencesKey(basePreferencesKeyName),
        defaultValue = defaultIsEnabled,
    )

internal val WidgetWifiProperty.IPProperty.SubProperty.isEnabledDse
    get() =
        DataStoreEntry.UniType.Impl(
            preferencesKey = booleanPreferencesKey("${property.basePreferencesKeyName}.${kind.basePreferencesKeyName}"),
            defaultValue = true,
        )

internal val WidgetButton.isEnabledDSE
    get() = DataStoreEntry.UniType.Impl(
        preferencesKey = booleanPreferencesKey("WidgetButton.$basePreferencesKeyName"),
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