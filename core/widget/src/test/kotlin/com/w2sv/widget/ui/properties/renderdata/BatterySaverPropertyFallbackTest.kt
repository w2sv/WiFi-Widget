package com.w2sv.widget.ui.properties.renderdata

import com.w2sv.domain.networking.NetworkAvailabilitySnapshot
import com.w2sv.domain.wifiproperty.WifiProperty
import com.w2sv.domain.wifiproperty.WifiPropertyResolutionError
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class BatterySaverPropertyFallbackTest {
    private val httpProperties = listOf(
        WifiProperty.PublicIp,
        WifiProperty.Location,
        WifiProperty.IpCoordinates,
        WifiProperty.ISP,
        WifiProperty.ASN
    )
    private val linkProperties = listOf(
        WifiProperty.ULA,
        WifiProperty.GUA,
        WifiProperty.LoopbackIp,
        WifiProperty.SiteLocalIp,
        WifiProperty.LinkLocalIp,
        WifiProperty.MulticastIp,
        WifiProperty.NAT64Prefix
    )

    @Test
    fun `connected Wi-Fi with no usable default network restricts HTTP and link properties`() {
        val availability = availability(true, false, false, true)

        (httpProperties + linkProperties).forEach { property ->
            assertEquals(
                WifiPropertyResolutionError.BatterySaverRestricted,
                availability.batterySaverPlaceholderFor(property)?.resolutionError
            )
        }
    }

    @Test
    fun `usable default network without link properties restricts only system IP properties`() {
        val availability = availability(true, true, false, true)

        httpProperties.forEach { assertNull(availability.batterySaverPlaceholderFor(it)) }
        linkProperties.forEach {
            assertEquals(WifiPropertyResolutionError.BatterySaverRestricted, availability.batterySaverPlaceholderFor(it)?.resolutionError)
        }
    }

    @Test
    fun `unavailable capabilities or absent internet capability restrict remote properties`() {
        val availability = availability(true, false, true, true)

        httpProperties.forEach {
            assertEquals(WifiPropertyResolutionError.BatterySaverRestricted, availability.batterySaverPlaceholderFor(it)?.resolutionError)
        }
        linkProperties.forEach { assertNull(availability.batterySaverPlaceholderFor(it)) }
    }

    @Test
    fun `accessible link properties may legitimately lack an address type`() {
        val availability = availability(true, true, true, true)

        (httpProperties + linkProperties).forEach { assertNull(availability.batterySaverPlaceholderFor(it)) }
    }

    @Test
    fun `Battery Saver alone or disconnected Wi-Fi does not create placeholders`() {
        val states = listOf(
            availability(true, false, false, false),
            availability(false, false, false, true)
        )

        states.forEach { availability ->
            (httpProperties + linkProperties).forEach { assertNull(availability.batterySaverPlaceholderFor(it)) }
        }
    }

    @Test
    fun `unaffected local and DHCP properties remain omitted`() {
        val availability = availability(true, false, false, true)
        val unaffected = listOf(
            WifiProperty.SSID,
            WifiProperty.BSSID,
            WifiProperty.Frequency,
            WifiProperty.Channel,
            WifiProperty.SignalStrength,
            WifiProperty.RSSI,
            WifiProperty.LinkSpeed,
            WifiProperty.DNS,
            WifiProperty.Gateway,
            WifiProperty.DHCP
        )

        unaffected.forEach { assertNull(availability.batterySaverPlaceholderFor(it)) }
    }

    private fun availability(
        isBatterySaverEnabled: Boolean,
        hasUsableDefaultNetwork: Boolean,
        hasLinkProperties: Boolean,
        isWifiConnected: Boolean
    ): NetworkAvailabilitySnapshot =
        NetworkAvailabilitySnapshot(
            isBatterySaverEnabled,
            hasUsableDefaultNetwork,
            hasLinkProperties,
            isWifiConnected
        )
}
