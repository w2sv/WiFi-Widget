package com.w2sv.networking.model

import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import org.junit.Test
import kotlin.test.assertTrue

class IPAddressTest {

    @Test
    fun `test fetchPublic`() {
        val httpClient = OkHttpClient()
        runTest {
            val v4FetchResult = IPAddress.fetchPublic(httpClient, IPAddress.Version.V4)
            val v6FetchResult = IPAddress.fetchPublic(httpClient, IPAddress.Version.V6)

            assertTrue(
                v4FetchResult.isSuccess || v6FetchResult.isSuccess,
                "Fetching of both IP versions failed with ${v4FetchResult.exceptionOrNull()} and ${v6FetchResult.exceptionOrNull()}"
            )
        }
    }
}
