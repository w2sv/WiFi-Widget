package com.w2sv.networking.remotenetworkinfo

import com.w2sv.domain.model.networking.IpApiData
import com.w2sv.domain.model.networking.RemoteWifiData
import com.w2sv.domain.model.widget.WidgetConfig
import com.w2sv.domain.model.wifiproperty.WifiProperty
import org.junit.Assert.assertEquals
import org.junit.Test

class RemoteDataRetentionTest {
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

    @Test fun `configuration filtering removes disabled cached properties`() {
        val previous = noRemoteProperties
            .withUpdatedPropertyEnablement(WifiProperty.Location, true)
            .withUpdatedPropertyEnablement(WifiProperty.ISP, true)
            .remoteDataConfig
        val current = noRemoteProperties.withUpdatedPropertyEnablement(WifiProperty.ISP, true).remoteDataConfig
        val data = RemoteWifiData(
            IpApiData("Berlin", null, "UTC", "ISP", "AS123"),
            emptyList()
        )
        assertEquals("ISP", current.retainApplicable(data, previous).ipApiData?.isp)
        assertEquals(null, current.retainApplicable(data, previous).ipApiData?.location)
        assertEquals(null, current.retainApplicable(data, previous).ipApiData?.asn)
    }
}
