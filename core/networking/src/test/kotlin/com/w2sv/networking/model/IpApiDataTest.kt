package com.w2sv.networking.model

import com.w2sv.domain.model.LocationParameter
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import org.junit.Test
import kotlin.test.assertNotNull

class IpApiDataTest {

    @Test
    fun `test fetch`() =
        runTest {
            IpApiData.fetch(client = OkHttpClient()).getOrThrow().also { println(it) }
                .run {
                    assertNotNull(asn)
                    assertNotNull(location(LocationParameter.entries))
                    assertNotNull(gpsCoordinates)
                }
        }
}
