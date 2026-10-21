package com.w2sv.networking.remotenetworkdata

import com.w2sv.domain.networking.IpApiData
import com.w2sv.domain.networking.RemoteNetworkData
import com.w2sv.domain.widget.WidgetConfig
import com.w2sv.domain.wifiproperty.WifiProperty
import org.junit.Assert.assertEquals
import org.junit.Test

class RemoteDataRetentionTest {
    private val defaultConfig = WidgetConfig.default
    private val noRemoteProperties = listOf(
        WifiProperty.Location,
        WifiProperty.IpCoordinates,
        WifiProperty.ISP,
        WifiProperty.ASN,
        WifiProperty.PublicIp
    ).fold(defaultConfig) { config, property ->
        config.withUpdatedPropertyEnablement(property, false)
    }

    @Test fun `configuration filtering removes disabled cached properties`() {
        val previous = noRemoteProperties
            .withUpdatedPropertyEnablement(WifiProperty.Location, true)
            .withUpdatedPropertyEnablement(WifiProperty.ISP, true)
            .remoteDataConfig
        val current = noRemoteProperties.withUpdatedPropertyEnablement(WifiProperty.ISP, true).remoteDataConfig
        val data = RemoteNetworkData(
            IpApiData("Berlin", null, "UTC", "ISP", "AS123"),
            emptyList()
        )
        assertEquals("ISP", current.retainApplicable(data, previous).ipApiData?.isp)
        assertEquals(null, current.retainApplicable(data, previous).ipApiData?.location)
        assertEquals(null, current.retainApplicable(data, previous).ipApiData?.asn)
    }
}
