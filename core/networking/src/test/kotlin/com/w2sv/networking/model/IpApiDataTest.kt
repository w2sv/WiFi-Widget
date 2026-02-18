package com.w2sv.networking.model

import com.w2sv.domain.model.wifiproperty.settings.LocationParameter
import com.w2sv.networking.fetching.ip_api.IpApiResponse
import kotlin.test.assertNotNull
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import org.junit.Test

class IpApiDataTest {

    @Test
    fun `test fetch`() =
        runTest {
            IpApiResponse.fetch(client = OkHttpClient()).getOrThrow().also { println(it) }
                .run {
                    assertNotNull(asn)
                    assertNotNull(location(LocationParameter.entries))
                    assertNotNull(gpsCoordinates)
                }
        }
}
