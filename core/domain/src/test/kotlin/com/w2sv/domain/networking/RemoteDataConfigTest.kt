package com.w2sv.domain.networking

import com.w2sv.domain.widget.WidgetConfig
import com.w2sv.domain.wifiproperty.WifiProperty
import com.w2sv.domain.wifiproperty.settings.IpSetting
import com.w2sv.domain.wifiproperty.settings.LocationParameter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RemoteDataConfigTest {
    private val defaultConfig = WidgetConfig.default
    private val noRemoteProperties = listOf(
        WifiProperty.Location,
        WifiProperty.IpGpsLocation,
        WifiProperty.ISP,
        WifiProperty.ASN,
        WifiProperty.PublicIp
    ).fold(defaultConfig) { config, property ->
        config.withUpdatedPropertyEnablement(property, false)
    }

    @Test fun `enabled properties determine remote groups and parameters`() {
        val disabled = noRemoteProperties.remoteDataConfig
        assertFalse(disabled.requiresIpApi)
        assertFalse(disabled.requiresPublicIp)
        assertTrue(disabled.locationParameters.isEmpty())

        val config = noRemoteProperties
            .withUpdatedPropertyEnablement(WifiProperty.Location, true)
            .withUpdatedPropertyEnablement(WifiProperty.ISP, true)
            .withUpdatedPropertyEnablement(WifiProperty.PublicIp, true)
            .withUpdatedPropertyConfig(WifiProperty.Location) {
                it.copy(settings = it.settings + (LocationParameter.ZipCode to false))
            }
            .withUpdatedPropertyConfig(WifiProperty.PublicIp) {
                it.copy(settings = it.settings + (IpSetting.V6Enabled to false))
            }
        val remoteConfig = config.remoteDataConfig
        assertEquals(setOf(WifiProperty.Location, WifiProperty.ISP), remoteConfig.ipApiProperties)
        assertEquals(setOf(IpAddress.Version.V4), remoteConfig.publicIpVersions)
        assertEquals(LocationParameter.entries.toSet() - LocationParameter.ZipCode, remoteConfig.locationParameters)
    }

    @Test fun `presentation and refresh settings do not change remote requirements`() {
        val original = defaultConfig.remoteDataConfig
        val reordered = defaultConfig.withUpdatedPropertyPosition(0, 1)
        val local = defaultConfig.withUpdatedPropertyEnablement(WifiProperty.SSID, true)
        val appearance = defaultConfig.copy(appearance = defaultConfig.appearance.copy(backgroundOpacity = 0.5f))
        val refreshing = defaultConfig.copy(refreshing = defaultConfig.refreshing.copy(refreshPeriodically = false))
        listOf(reordered, local, appearance, refreshing).forEach {
            assertEquals(original, it.remoteDataConfig)
        }
    }
}
