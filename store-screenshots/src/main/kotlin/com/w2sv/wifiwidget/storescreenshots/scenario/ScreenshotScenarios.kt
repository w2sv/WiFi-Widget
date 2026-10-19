package com.w2sv.wifiwidget.storescreenshots.scenario

import android.graphics.Color
import com.w2sv.common.txt
import com.w2sv.domain.widget.WidgetAppearance
import com.w2sv.domain.widget.WidgetColoring
import com.w2sv.domain.widget.WidgetColoringStrategy
import com.w2sv.domain.widget.WidgetColors
import com.w2sv.domain.widget.WidgetUtility
import com.w2sv.domain.wifiproperty.ResolvedWifiProperty
import com.w2sv.domain.wifiproperty.WifiProperty
import com.w2sv.domain.wifiproperty.WifiPropertyValue
import com.w2sv.wifiwidget.storescreenshots.R
import com.w2sv.wifiwidget.storescreenshots.device.GridCells

object ScreenshotScenarios {

    init {
        // Initialize entries before its nested property singletons
        WifiProperty.entries
    }

    val networkOverview = ScreenshotScenario(
        name = "01-widget-network-overview.png",
        wallpaperRes = R.drawable.road,
        widgetSizes = ScreenshotWidgetSizes(GridCells(5, 2), GridCells(5, 3), GridCells(4, 3)),
        properties = listOf(
            resolved(WifiProperty.SSID, "Martin Router King"),
            resolved(WifiProperty.BSSID, "02:11:22:33:44:55"),
            resolved(WifiProperty.SiteLocalIp, "192.168.1.42", "/24", "255.255.255.0"),
            resolved(WifiProperty.PublicIp, "203.0.113.42"),
            resolved(WifiProperty.Frequency, "5180 MHz"),
            resolved(WifiProperty.Channel, "36"),
            resolved(WifiProperty.LinkSpeed, "1201 Mbps"),
            resolved(WifiProperty.SignalStrength, "Excellent")
        ),
        appearance = WidgetAppearance(
            coloring = WidgetColoring(
                custom = WidgetColoringStrategy.Custom(
                    WidgetColors(
                        background = Color.rgb(249, 250, 244),
                        primary = Color.rgb(58, 110, 77),
                        secondary = Color.rgb(43, 59, 48)
                    )
                ),
                useCustom = true
            ),
            backgroundOpacity = 0.8f
        ),
        enabledUtilities = WidgetUtility.entries
    )

    val customizedWidget = ScreenshotScenario(
        name = "03-widget-customized.png",
        wallpaperRes = R.drawable.brush_strokes_d,
        widgetSizes = ScreenshotWidgetSizes(GridCells(4, 2), GridCells(4, 2), GridCells(3, 2)),
        properties = listOf(
            resolved(WifiProperty.SSID, "Looking for Wife-Fi"),
            resolved(WifiProperty.Standard, "802.11ax"),
            resolved(WifiProperty.Security, "WPA3-Personal"),
            resolved(WifiProperty.RSSI, "-49 dBm"),
            resolved(WifiProperty.Gateway, "192.168.1.1"),
            resolved(WifiProperty.DNS, "1.1.1.1"),
            // Illustrative IP-derived geolocation, never the device's actual GPS position.
            resolved(WifiProperty.Location, "Berlin, Germany"),
            resolved(WifiProperty.ISP, "ExampleNet"),
            resolved(WifiProperty.ASN, "AS64512")
        ),
        appearance = WidgetAppearance(
            coloring = WidgetColoring(
                custom = WidgetColoringStrategy.Custom(
                    WidgetColors(
                        background = Color.rgb(58, 39, 53),
                        primary = Color.rgb(245, 174, 151),
                        secondary = Color.rgb(250, 240, 231)
                    )
                ),
                useCustom = true
            )
        ),
        enabledUtilities = listOf(WidgetUtility.RefreshTimeDisplay, WidgetUtility.RefreshButton)
    )

    val transparentWidget = ScreenshotScenario(
        name = "05-widget-transparent.png",
        wallpaperRes = R.drawable.abstract_magenta_indigo,
        widgetSizes = ScreenshotWidgetSizes(GridCells(4, 2), GridCells(3, 2), GridCells(2, 2)),
        properties = listOf(
            resolved(WifiProperty.SSID, "Walter White-Fi"),
            resolved(WifiProperty.SiteLocalIp, "192.168.1.42", "/24", "255.255.255.0"),
            resolved(WifiProperty.PublicIp, "203.0.113.42"),
            resolved(WifiProperty.ULA, "fd12:3456:789a::42", "/64"),
            resolved(WifiProperty.GUA, "2001:db8:1::42")
        ),
        appearance = WidgetAppearance(
            coloring = WidgetColoring(
                custom = WidgetColoringStrategy.Custom(
                    WidgetColors(
                        background = Color.rgb(23, 16, 39),
                        primary = Color.rgb(255, 191, 212),
                        secondary = Color.rgb(255, 248, 242)
                    )
                ),
                useCustom = true
            ),
            backgroundOpacity = 0.60f
        )
    )

    val all = listOf(networkOverview, customizedWidget, transparentWidget)

    private fun resolved(
        property: WifiProperty,
        value: String,
        vararg subValues: String
    ): ResolvedWifiProperty =
        ResolvedWifiProperty(
            property,
            listOf(WifiPropertyValue(value = value.txt, subValues = subValues.toList()))
        )
}
