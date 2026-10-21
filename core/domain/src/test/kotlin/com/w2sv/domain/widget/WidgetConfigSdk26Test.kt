package com.w2sv.domain.widget

import com.w2sv.domain.wifiproperty.WifiProperty
import kotlin.test.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [26])
class WidgetConfigSdk26Test {

    @Test
    fun `supportedProperties does not include unsupported properties`() {
        val actual = WidgetConfig.default.supportedProperties
        assertEquals(
            listOf(
                WifiProperty.SSID,
                WifiProperty.BSSID,
                WifiProperty.LoopbackIp,
                WifiProperty.SiteLocalIp,
                WifiProperty.LinkLocalIp,
                WifiProperty.ULA,
                WifiProperty.MulticastIp,
                WifiProperty.GUA,
                WifiProperty.PublicIp,
                WifiProperty.Frequency,
                WifiProperty.Channel,
                WifiProperty.LinkSpeed,
                WifiProperty.RSSI,
                WifiProperty.SignalStrength,
                WifiProperty.Gateway,
                WifiProperty.DNS,
                WifiProperty.DHCP,
                WifiProperty.Location,
                WifiProperty.IpCoordinates,
                WifiProperty.ISP,
                WifiProperty.ASN
            ),
            actual
        )
    }
}
