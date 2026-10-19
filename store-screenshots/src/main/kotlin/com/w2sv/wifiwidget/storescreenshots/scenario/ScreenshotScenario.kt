package com.w2sv.wifiwidget.storescreenshots.scenario

import androidx.annotation.DrawableRes
import com.w2sv.domain.widget.WidgetAppearance
import com.w2sv.domain.widget.WidgetConfig
import com.w2sv.domain.widget.WidgetUtility
import com.w2sv.domain.wifiproperty.ResolvedWifiProperty
import com.w2sv.domain.wifiproperty.WifiProperty
import com.w2sv.domain.wifiproperty.settings.IpSetting
import com.w2sv.wifiwidget.storescreenshots.device.GridCells
import com.w2sv.wifiwidget.storescreenshots.device.ScreenshotDeviceCategory

data class ScreenshotWidgetSizes(val phone: GridCells, val tablet: GridCells, val largeTablet: GridCells) {
    fun forDevice(category: ScreenshotDeviceCategory): GridCells =
        when (category) {
            ScreenshotDeviceCategory.Phone -> phone
            ScreenshotDeviceCategory.Tablet -> tablet
            ScreenshotDeviceCategory.LargeTablet -> largeTablet
        }
}

data class ScreenshotScenario(
    val name: String,
    @DrawableRes val wallpaperRes: Int,
    val widgetSizes: ScreenshotWidgetSizes,
    val properties: List<ResolvedWifiProperty>,
    val appearance: WidgetAppearance,
    val enabledUtilities: Iterable<WidgetUtility> = emptyList()
) {
    val enabledProperties: List<WifiProperty>
        get() = properties.map { it.property }

    val widgetConfig: WidgetConfig
        get() {
            val enabled = enabledProperties.toSet()
            val selectedProperties = WifiProperty.entries.fold(WidgetConfig.default) { config, property ->
                config.withUpdatedPropertyEnablement(property, property in enabled)
            }
            // Hide unused subvalues, and disable IPv6 only where the property has a version setting.
            val compactIpProperties = enabledProperties.filterIsInstance<WifiProperty.IpProperty>()
                .fold(selectedProperties) { config, property ->
                    config.withUpdatedPropertyConfig(property) { propertyConfig ->
                        propertyConfig.copy(
                            settings = propertyConfig.settings.mapValues { (setting, enabled) ->
                                when (setting) {
                                    IpSetting.ShowPrefixLength, IpSetting.ShowSubnetMask, IpSetting.V6Enabled -> false
                                    else -> enabled
                                }
                            }
                        )
                    }
                }
            return compactIpProperties.copy(
                propertyOrder = enabledProperties + WifiProperty.entries.filterNot { it in enabled },
                appearance = appearance,
                refreshing = WidgetConfig.default.refreshing.copy(refreshPeriodically = false),
                utilities = WidgetUtility.entries.associateWith { it in enabledUtilities }
            )
        }
}
