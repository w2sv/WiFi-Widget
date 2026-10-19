package com.w2sv.networking.remotenetworkdata

import com.w2sv.domain.networking.IpAddress
import com.w2sv.networking.remotenetworkdata.ipapi.IpApiDataFetcher
import com.w2sv.networking.remotenetworkdata.publicip.PublicIpFetcher
import io.mockk.every
import io.mockk.mockk
import java.util.concurrent.CancellationException
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import org.junit.Assert.assertTrue
import org.junit.Test

class RemoteFetcherCancellationTest {
    private val client = mockk<OkHttpClient> {
        every { newCall(any()) } throws CancellationException("cancelled")
    }

    @Test fun `IP-API fetch propagates cancellation from HTTP`() =
        runTest {
            val result = runCatching { IpApiDataFetcher(client, Json.Default).fetch(emptySet()) }
            assertTrue(result.exceptionOrNull() is CancellationException)
        }

    @Test fun `public IP fetch propagates cancellation from HTTP`() =
        runTest {
            val result = runCatching { PublicIpFetcher(client).fetch(setOf(IpAddress.Version.V4)) }
            assertTrue(result.exceptionOrNull() is CancellationException)
        }
}
