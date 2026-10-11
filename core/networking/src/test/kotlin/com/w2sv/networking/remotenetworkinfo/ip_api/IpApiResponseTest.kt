package com.w2sv.networking.remotenetworkinfo.ip_api

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlinx.serialization.json.Json

class IpApiResponseTest {
    @Test
    fun `missing coordinate does not produce a location`() {
        val response = Json.decodeFromString<IpApiResponse>("""{"lat":-1.0}""")

        assertNull(response.toDomain(emptyList()).gpsCoordinates)
    }

    @Test
    fun `negative one coordinate remains a valid location`() {
        val response = Json.decodeFromString<IpApiResponse>("""{"lat":-1.0,"lon":2.5}""")

        assertEquals("-1.0, 2.5", response.toDomain(emptyList()).gpsCoordinates)
    }
}
