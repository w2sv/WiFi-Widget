package com.w2sv.datastore.proto.migration

import android.content.Context
import androidx.datastore.core.DataMigration
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.w2sv.common.utils.log
import com.w2sv.datastore.WidgetColoringProto
import com.w2sv.datastore.WidgetConfigProto
import com.w2sv.datastore.proto.di.widgetColoringProtoFile
import com.w2sv.datastore.proto.mapping.toExternal
import com.w2sv.datastore.proto.mapping.toProto
import com.w2sv.domain.model.widget.FontSize
import com.w2sv.domain.model.widget.WidgetColoring
import com.w2sv.domain.model.widget.WidgetConfig
import com.w2sv.domain.model.widget.WidgetUtility
import com.w2sv.domain.model.widget.WifiPropertyValueAlignment
import com.w2sv.domain.model.wifiproperty.WifiProperty
import com.w2sv.domain.model.wifiproperty.settings.LocationParameter
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlin.time.Duration.Companion.minutes
import kotlinx.coroutines.flow.first
import slimber.log.i

private val MIGRATION_DONE_PREFERENCES_KEY = booleanPreferencesKey("widget_config_migrated")

internal class WidgetConfigMigration @Inject constructor(
    @ApplicationContext private val context: Context,
    private val preferences: DataStore<Preferences>,
    private val coloringDataStore: DataStore<WidgetColoringProto>
) : DataMigration<WidgetConfigProto> {

    override suspend fun shouldMigrate(currentData: WidgetConfigProto): Boolean =
        (preferences.data.first()[MIGRATION_DONE_PREFERENCES_KEY] != true).log { "shouldMigrate=$it" }

    override suspend fun migrate(currentData: WidgetConfigProto): WidgetConfigProto {
        val prefs = preferences.data.first()
        val coloring = coloringDataStore.data.first().toExternal()

        i { "Performing migration; prefs=$prefs" }

        return WidgetConfig
            .default
            .migratePropertyOrder(prefs)
            .migratePropertyEnablement(prefs)
            .migrateIpSettings(prefs)
            .migrateLocationParameters(prefs)
            .migrateUtilities(prefs)
            .migrateAppearance(prefs, coloring)
            .migrateRefreshing(prefs)
            .toProto()
    }

    override suspend fun cleanUp() {
        preferences.edit { it[MIGRATION_DONE_PREFERENCES_KEY] = true }
        val file = context.widgetColoringProtoFile()
        if (file.exists()) {
            file.delete()
        }
    }
}

private fun WidgetConfig.migratePropertyOrder(prefs: Preferences): WidgetConfig {
    val key = stringPreferencesKey("wifiPropertyOrder")

    val parsed = prefs[key]
        ?.split(",")
        ?.mapNotNull { it.toIntOrNull() }
        ?.mapNotNull { WifiProperty.entries.getOrNull(it) }
        ?.distinct()
        .orEmpty()

    if (parsed.isEmpty()) return this

    val missing = WifiProperty.entries - parsed.toSet()
    val finalOrder = parsed + missing

    return copy(propertyOrder = finalOrder)
}

private fun WidgetConfig.migratePropertyEnablement(prefs: Preferences): WidgetConfig {
    var config = this

    WifiProperty.entries.forEach { property ->
        val value = prefs[booleanPreferencesKey(property.legacyPreferencesKeyName)] ?: return@forEach
        config = config.withUpdatedPropertyEnablement(property, value)
    }

    return config
}

private fun WidgetConfig.migrateIpSettings(prefs: Preferences): WidgetConfig {
    var config = this

    WifiProperty.entries.forEach { property ->
        if (property is WifiProperty.IpProperty) {
            property.settings.forEach { setting ->
                val key = booleanPreferencesKey("${property.legacyPreferencesKeyName}.${setting.name}")
                val value = prefs[key] ?: return@forEach

                config = config.withUpdatedPropertyConfig(property) {
                    it.withUpdatedSetting(setting, value)
                }
            }
        }
    }

    return config
}

private fun WidgetConfig.migrateLocationParameters(prefs: Preferences): WidgetConfig {
    var config = this

    LocationParameter.entries.forEach { param ->
        val key = booleanPreferencesKey(param.name)
        val value = prefs[key] ?: return@forEach

        config = config.withUpdatedPropertyConfig(WifiProperty.Location) {
            it.withUpdatedSetting(param, value)
        }
    }

    return config
}

private fun WidgetConfig.migrateAppearance(prefs: Preferences, coloring: WidgetColoring): WidgetConfig {
    val opacity = prefs[floatPreferencesKey("opacity")]
    val fontSize = prefs[intPreferencesKey("fontSize")]?.let { FontSize.entries[it] }
    val alignment = prefs[intPreferencesKey("propertyValueAlignment")]
        ?.let { WifiPropertyValueAlignment.entries[it] }

    return copy(
        appearance = appearance.copy(
            coloring = coloring,
            backgroundOpacity = opacity ?: appearance.backgroundOpacity,
            fontSize = fontSize ?: appearance.fontSize,
            propertyValueAlignment = alignment ?: appearance.propertyValueAlignment
        )
    )
}

private fun WidgetConfig.migrateUtilities(prefs: Preferences): WidgetConfig {
    val updated = WidgetUtility.entries.associateWith { utility ->
        val key = booleanPreferencesKey(
            when (utility) {
                WidgetUtility.RefreshTimeDisplay -> "ShowDateTime"
                WidgetUtility.RefreshButton -> "WidgetButton.Refresh"
                WidgetUtility.GoToWifiSettingsButton -> "WidgetButton.GoToWifiSettings"
                WidgetUtility.GoToWidgetSettingsButton -> "WidgetButton.GoToWidgetSettings"
            }
        )

        prefs[key] ?: true
    }

    return copy(utilities = updated)
}

private fun WidgetConfig.migrateRefreshing(prefs: Preferences): WidgetConfig {
    val refreshPeriodically = prefs[booleanPreferencesKey("RefreshPeriodically")]
    val refreshOnLowBattery = prefs[booleanPreferencesKey("RefreshOnBatteryLow")]
    val intervalMinutes = prefs[intPreferencesKey("refreshInterval")]

    return copy(
        refreshing = refreshing.copy(
            refreshPeriodically = refreshPeriodically ?: refreshing.refreshPeriodically,
            refreshOnLowBattery = refreshOnLowBattery ?: refreshing.refreshOnLowBattery,
            interval = intervalMinutes?.minutes ?: refreshing.interval
        )
    )
}
