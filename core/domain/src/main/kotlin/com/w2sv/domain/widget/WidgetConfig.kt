package com.w2sv.domain.widget

import com.w2sv.domain.networking.RemoteDataConfig
import com.w2sv.domain.wifiproperty.WifiProperty
import com.w2sv.domain.wifiproperty.WifiPropertyConfig
import com.w2sv.domain.wifiproperty.settings.IpSetting
import com.w2sv.domain.wifiproperty.settings.LocationParameter
import com.w2sv.domain.wifiproperty.settings.WifiPropertySetting
import com.w2sv.kotlinutils.copy
import com.w2sv.kotlinutils.threadUnsafeLazy
import com.w2sv.kotlinutils.trueKeys
import com.w2sv.kotlinutils.update

data class WidgetConfig(
    val propertyConfig: Map<WifiProperty, WifiPropertyConfig<WifiPropertySetting>>,
    val propertyOrder: List<WifiProperty>,
    val appearance: WidgetAppearance,
    val refreshing: WidgetRefreshing,
    val utilities: Map<WidgetUtility, Boolean>
) {
    val remoteDataConfig: RemoteDataConfig
        get() = RemoteDataConfig.from(this)

    val supportedProperties by threadUnsafeLazy { propertyOrder.filter { it.isSupported } }

    val enabledProperties by threadUnsafeLazy { supportedProperties.filter { isEnabled(it) } }

    val arePropertiesInDefaultOrder: Boolean
        get() = propertyOrder == WifiProperty.entries

    val isAnyLocationAccessRequiringPropertyEnabled: Boolean
        get() = WifiProperty.locationAccessRequiring.any { property -> isEnabled(property) }

    fun isEnabled(property: WifiProperty): Boolean =
        propertyConfig(property).isEnabled

    // ================
    // Copying
    // ================

    fun withUpdatedPropertyPosition(from: Int, to: Int): WidgetConfig =
        copy(propertyOrder = propertyOrder.copy { add(to, removeAt(from)) })

    fun withDefaultPropertyOrder(): WidgetConfig =
        copy(propertyOrder = WifiProperty.entries)

    fun withUpdatedPropertyConfig(
        property: WifiProperty,
        transform: (WifiPropertyConfig<WifiPropertySetting>) -> WifiPropertyConfig<WifiPropertySetting>
    ): WidgetConfig =
        copy(propertyConfig = propertyConfig.copy { update(property, transform) })

    fun withUpdatedPropertyEnablement(property: WifiProperty, isEnabled: Boolean): WidgetConfig =
        withUpdatedPropertyConfig(property) { it.copy(isEnabled = isEnabled) }

    fun withEnabledLocationAccessRequiringProperties(): WidgetConfig =
        copy(
            propertyConfig = propertyConfig.copy {
                WifiProperty.locationAccessRequiring.forEach { property ->
                    update(property) { it.copy(isEnabled = true) }
                }
            }
        )

    // ================
    // Property Config Access
    // ================

    fun propertyConfig(property: WifiProperty): WifiPropertyConfig<WifiPropertySetting> =
        propertyConfig.getValue(property)

    fun ipPropertyConfig(property: WifiProperty.IpProperty): WifiPropertyConfig<IpSetting> =
        typedConfig(property)

    fun enabledIpSettings(property: WifiProperty.IpProperty): List<IpSetting> =
        ipPropertyConfig(property).enabledSettings

    fun enabledLocationParameters(): List<LocationParameter> =
        typedConfig<LocationParameter>(WifiProperty.Location).enabledSettings

    @Suppress("UNCHECKED_CAST")
    private fun <S : WifiPropertySetting> typedConfig(property: WifiProperty): WifiPropertyConfig<S> =
        propertyConfig(property) as WifiPropertyConfig<S>

    // ================
    // WidgetUtility
    // ================
    fun enabledUtilities(): List<WidgetUtility> =
        utilities.trueKeys()

    companion object {
        val default = WidgetConfig(
            propertyConfig = WifiProperty.entries.associateWith { property ->
                WifiPropertyConfig(
                    isEnabled = property.isEnabledByDefault,
                    settings = when (property) {
                        is WifiProperty.Location -> LocationParameter.entries.associateWith { true }
                        is WifiProperty.IpProperty -> property.settings.associateWith { true }
                        else -> emptyMap()
                    }
                )
            },
            propertyOrder = WifiProperty.entries,
            appearance = WidgetAppearance(),
            refreshing = WidgetRefreshing(),
            utilities = WidgetUtility.entries.associateWith { true }
        )
    }
}
