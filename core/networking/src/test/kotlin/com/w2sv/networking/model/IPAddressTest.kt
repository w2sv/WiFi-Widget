package com.w2sv.networking.model

import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import org.junit.Test

class IPAddressTest {

    @Test
    fun `test fetchPublic`() {
        val httpClient = OkHttpClient()
        runTest {
            IPAddress.fetchPublic(httpClient, IPAddress.Version.V4).getOrThrow()
            IPAddress.fetchPublic(httpClient, IPAddress.Version.V6).getOrThrow()
        }
    }
}
