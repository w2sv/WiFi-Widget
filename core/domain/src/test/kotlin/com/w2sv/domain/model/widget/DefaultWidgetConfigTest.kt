package com.w2sv.domain.model.widget

import com.w2sv.androidutils.os.dynamicColorsSupported
import com.w2sv.domain.model.Theme
import com.w2sv.domain.model.wifiproperty.WifiProperty
import com.w2sv.domain.model.wifiproperty.WifiPropertyConfig
import com.w2sv.domain.model.wifiproperty.settings.IpSetting
import com.w2sv.domain.model.wifiproperty.settings.LocationParameter
import com.w2sv.domain.model.wifiproperty.settings.WifiPropertySetting
import org.junit.Test
import kotlin.test.assertEquals

class DefaultWidgetConfigTest {

    private val defaultConfig = WidgetConfig.default

    @Test
    fun propertyOrder() {
        assertEquals(WifiProperty.entries, defaultConfig.propertyOrder)
    }

    @Test
    fun propertyConfig() {
        assertEquals(defaultPropertyConfig, defaultConfig.propertyConfigMap)
    }

    @Test
    fun appearance() {
        assertEquals(defaultAppearance, defaultConfig.appearance)
    }
}

private val defaultAppearance = WidgetAppearance(
    coloring = WidgetColoring(
        preset = WidgetColoringStrategy.Preset(
            theme = Theme.Default,
            useDynamicColors = dynamicColorsSupported
        ),
        custom = WidgetColoringStrategy.Custom(
            WidgetColors(
                background = -7859146,
                primary = -5898336,
                secondary = -1
            )
        ),
        useCustom = false
    ),
    backgroundOpacity = 1f,
    fontSize = FontSize.Medium,
    propertyValueAlignment = WifiPropertyValueAlignment.Left
)

private val defaultPropertyConfig: Map<WifiProperty, WifiPropertyConfig<WifiPropertySetting>> = mapOf(
    WifiProperty.SSID to WifiPropertyConfig(false, emptyMap()),
    WifiProperty.BSSID to WifiPropertyConfig(false, emptyMap()),
    WifiProperty.LoopbackIp to WifiPropertyConfig(
        true,
        mapOf(
            IpSetting.V4Enabled to true,
            IpSetting.V6Enabled to true,
            IpSetting.ShowPrefixLength to true,
            IpSetting.ShowSubnetMask to true
        )
    ),
    WifiProperty.SiteLocalIp to WifiPropertyConfig(
        true,
        mapOf(
            IpSetting.V4Enabled to true,
            IpSetting.V6Enabled to true,
            IpSetting.ShowPrefixLength to true,
            IpSetting.ShowSubnetMask to true
        )
    ),
    WifiProperty.LinkLocalIp to WifiPropertyConfig(
        true,
        mapOf(
            IpSetting.V4Enabled to true,
            IpSetting.V6Enabled to true,
            IpSetting.ShowPrefixLength to true,
            IpSetting.ShowSubnetMask to true
        )
    ),
    WifiProperty.ULA to WifiPropertyConfig(
        true,
        mapOf(
            IpSetting.ShowPrefixLength to true
        )
    ),
    WifiProperty.MulticastIp to WifiPropertyConfig(
        true,
        mapOf(
            IpSetting.V4Enabled to true,
            IpSetting.V6Enabled to true,
            IpSetting.ShowPrefixLength to true,
            IpSetting.ShowSubnetMask to true
        )
    ),
    WifiProperty.GUA to WifiPropertyConfig(
        true,
        mapOf(
            IpSetting.ShowPrefixLength to true
        )
    ),
    WifiProperty.PublicIp to WifiPropertyConfig(
        true,
        mapOf(
            IpSetting.V4Enabled to true,
            IpSetting.V6Enabled to true
        )
    ),
    WifiProperty.Frequency to WifiPropertyConfig(true, emptyMap()),
    WifiProperty.Channel to WifiPropertyConfig(true, emptyMap()),
    WifiProperty.LinkSpeed to WifiPropertyConfig(true, emptyMap()),
    WifiProperty.RSSI to WifiPropertyConfig(true, emptyMap()),
    WifiProperty.SignalStrength to WifiPropertyConfig(true, emptyMap()),
    WifiProperty.Standard to WifiPropertyConfig(true, emptyMap()),
    WifiProperty.Generation to WifiPropertyConfig(true, emptyMap()),
    WifiProperty.Security to WifiPropertyConfig(true, emptyMap()),
    WifiProperty.Gateway to WifiPropertyConfig(true, emptyMap()),
    WifiProperty.DNS to WifiPropertyConfig(true, emptyMap()),
    WifiProperty.DHCP to WifiPropertyConfig(true, emptyMap()),
    WifiProperty.NAT64Prefix to WifiPropertyConfig(true, emptyMap()),
    WifiProperty.Location to WifiPropertyConfig(
        true,
        mapOf(
            LocationParameter.ZipCode to true,
            LocationParameter.District to true,
            LocationParameter.City to true,
            LocationParameter.Region to true,
            LocationParameter.Country to true,
            LocationParameter.Continent to true
        )
    ),

    WifiProperty.IpGpsLocation to WifiPropertyConfig(true, emptyMap()),
    WifiProperty.ISP to WifiPropertyConfig(true, emptyMap()),
    WifiProperty.ASN to WifiPropertyConfig(true, emptyMap())
)
