package com.w2sv.domain.widget

import com.w2sv.domain.wifiproperty.WifiProperty
import kotlin.test.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [26])
class WidgetConfigSupportedPropertyReorderingTest {
    private val order get() = listOf(
        WifiProperty.SSID,
        WifiProperty.Standard,
        WifiProperty.BSSID,
        WifiProperty.Generation,
        WifiProperty.RSSI,
        WifiProperty.Security
    )

    @Test
    fun `moving a supported property forward preserves unsupported slots`() {
        val updated = WidgetConfig.default.copy(propertyOrder = order)
            .withMovedSupportedProperty(WifiProperty.SSID, WifiProperty.RSSI)

        assertEquals(
            listOf(
                WifiProperty.BSSID,
                WifiProperty.Standard,
                WifiProperty.RSSI,
                WifiProperty.Generation,
                WifiProperty.SSID,
                WifiProperty.Security
            ),
            updated.propertyOrder
        )
    }

    @Test
    fun `moving a supported property backward preserves unsupported slots`() {
        val updated = WidgetConfig.default.copy(propertyOrder = order)
            .withMovedSupportedProperty(WifiProperty.RSSI, WifiProperty.SSID)

        assertEquals(
            listOf(
                WifiProperty.RSSI,
                WifiProperty.Standard,
                WifiProperty.SSID,
                WifiProperty.Generation,
                WifiProperty.BSSID,
                WifiProperty.Security
            ),
            updated.propertyOrder
        )
    }
}
