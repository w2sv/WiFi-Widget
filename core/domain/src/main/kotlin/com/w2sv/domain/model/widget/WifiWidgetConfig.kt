package com.w2sv.domain.model.widget

import com.w2sv.domain.model.wifiproperty.WifiProperty
import com.w2sv.domain.model.wifiproperty.WifiPropertyConfig
import com.w2sv.domain.model.wifiproperty.settings.IpSetting
import com.w2sv.domain.model.wifiproperty.settings.LocationParameter
import com.w2sv.domain.model.wifiproperty.settings.WifiPropertySetting
import com.w2sv.kotlinutils.copy
import com.w2sv.kotlinutils.trueKeys
import com.w2sv.kotlinutils.update

// TODO test
data class WifiWidgetConfig(
    val properties: Map<WifiProperty, WifiPropertyConfig<WifiPropertySetting>>,
    val orderedProperties: List<WifiProperty>,
    val appearance: WidgetAppearance,
    val refreshing: WidgetRefreshing,
    val bottomBarElements: Map<WidgetBottomBarElement, Boolean>
) {
    fun isEnabled(property: WifiProperty): Boolean =
        properties.getValue(property).isEnabled

    // TODO exclude unsupported properties
    fun orderedEnabledProperties(): List<WifiProperty> =
        orderedProperties.filter { isEnabled(it) }

    val propertiesInDefaultOrder: Boolean
        get() = orderedProperties == WifiProperty.entries

    val isAnyLocationAccessRequiringPropertyEnabled: Boolean
        get() = WifiProperty.locationAccessRequiring.any { property -> isEnabled(property) }

    fun withModifiedPropertyPosition(from: Int, to: Int): WifiWidgetConfig =
        copy(orderedProperties = orderedProperties.copy { add(to, removeAt(from)) })

    fun withDefaultPropertyOrder(): WifiWidgetConfig =
        copy(orderedProperties = WifiProperty.entries)

    fun withConfiguredPropertyEnablement(property: WifiProperty, isEnabled: Boolean): WifiWidgetConfig =
        copy(properties = properties.copy { update(property) { it.copy(isEnabled = isEnabled) } })

    fun withEnabledLocationAccessRequiringProperties(): WifiWidgetConfig =
        copy(
            properties = properties.copy {
                WifiProperty.locationAccessRequiring.forEach { property ->
                    update(property) { it.copy(isEnabled = true) }
                }
            }
        )

    // ================
    // Property Config Access
    // ================

    fun ipPropertyConfig(property: WifiProperty.IpProperty): WifiPropertyConfig<IpSetting> =
        typedConfig(property)

    fun enabledIpSettings(property: WifiProperty.IpProperty): List<IpSetting> =
        ipPropertyConfig(property).settings.trueKeys()

    val locationConfig: WifiPropertyConfig<LocationParameter>
        get() = typedConfig(WifiProperty.Location)

    fun enabledLocationParameters(): List<LocationParameter> =
        locationConfig.settings.trueKeys()

    @Suppress("UNCHECKED_CAST")
    private fun <S : WifiPropertySetting> typedConfig(property: WifiProperty): WifiPropertyConfig<S> =
        properties.getValue(property) as WifiPropertyConfig<S>

    // ================
    // BottomBar
    // ================

    fun bottomBar(): List<WidgetBottomBarElement> =
        bottomBarElements.trueKeys()

    companion object {
        val default = WifiWidgetConfig(
            properties = WifiProperty.entries.associateWith { property ->
                WifiPropertyConfig(
                    isEnabled = property.isEnabledDefault,
                    settings = when (property) {
                        is WifiProperty.Location -> LocationParameter.entries.associateWith { true }
                        is WifiProperty.IpProperty -> property.settings.associateWith { true }
                        else -> emptyMap()
                    }
                )
            },
            orderedProperties = WifiProperty.entries,
            appearance = WidgetAppearance(),
            refreshing = WidgetRefreshing(),
            bottomBarElements = WidgetBottomBarElement.entries.associateWith { true }
        )
    }
}
