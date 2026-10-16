package com.w2sv.networking.remotenetworkinfo

import com.w2sv.domain.model.networking.IpAddress
import com.w2sv.domain.model.networking.IpApiData
import com.w2sv.domain.model.networking.RemoteWifiData
import com.w2sv.domain.model.widget.WidgetConfig
import com.w2sv.domain.model.wifiproperty.WifiProperty
import com.w2sv.domain.model.wifiproperty.settings.IpSetting
import com.w2sv.domain.model.wifiproperty.settings.LocationParameter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RemoteDataSelectionTest {
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
        val disabled = RemoteDataSelection.from(noRemoteProperties)
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
        val selection = RemoteDataSelection.from(config)
        assertEquals(setOf(WifiProperty.Location, WifiProperty.ISP), selection.ipApiProperties)
        assertEquals(setOf(IpAddress.Version.V4), selection.publicIpVersions)
        assertEquals(LocationParameter.entries.toSet() - LocationParameter.ZipCode, selection.locationParameters)
    }

    @Test fun `configuration filtering removes disabled cached properties`() {
        val previous = RemoteDataSelection.from(
            noRemoteProperties
                .withUpdatedPropertyEnablement(WifiProperty.Location, true)
                .withUpdatedPropertyEnablement(WifiProperty.ISP, true)
        )
        val current = RemoteDataSelection.from(
            noRemoteProperties.withUpdatedPropertyEnablement(WifiProperty.ISP, true)
        )
        val data = RemoteWifiData(
            IpApiData("Berlin", null, "UTC", "ISP", "AS123"),
            emptyList()
        )
        assertEquals("ISP", current.retainApplicable(data, previous).ipApiData?.isp)
        assertEquals(null, current.retainApplicable(data, previous).ipApiData?.location)
        assertEquals(null, current.retainApplicable(data, previous).ipApiData?.asn)
    }
}
