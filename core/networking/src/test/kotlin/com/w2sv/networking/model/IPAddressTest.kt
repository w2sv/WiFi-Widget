package com.w2sv.networking.model

import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import org.junit.Test
import java.net.Inet4Address
import java.net.Inet6Address
import kotlin.test.assertEquals
import kotlin.test.assertFalse
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

    @Test
    fun `subnetMask should return correct mask for valid prefix lengths`() {
        val cases = listOf(
            // standard prefix lengths
            8 to "255.0.0.0",
            16 to "255.255.0.0",
            24 to "255.255.255.0",
            32 to "255.255.255.255",
            // non-standard prefix lengths
            0 to "0.0.0.0",
            10 to "255.192.0.0",
            18 to "255.255.192.0",
            26 to "255.255.255.192",
            30 to "255.255.255.252"
        )

        cases.forEach { (prefixLength, expectedMask) ->
            assertEquals(expectedMask, testIPv4Address(prefixLength = prefixLength).subnetMask)
        }
    }

    @Test
    fun `isUniqueLocal should return true for unique local addresses`() {
        // Test cases with unique local IPv6 addresses
        listOf(
            "fc00::1",
            "fd12:3456:789a:1::1",
            "fdff:ffff:ffff:ffff:ffff:ffff:ffff:ffff"
        )
            .forEach { address ->
                assertTrue(testIPv6Address(address).isUniqueLocal, "Expected $address to be unique local")
            }
    }

    @Test
    fun `isUniqueLocal should return false for non-unique local addresses`() {
        listOf(
            "2001:db8::1", // Global Unicast
            "fe80::1",     // Link-local
            "ff00::1",     // Multicast
            "::1",         // Loopback
            "2001::1"      // Global Unicast
        )
            .forEach { address ->
                assertFalse(testIPv6Address(address).isUniqueLocal, "Expected $address to not be unique local")
            }
    }
}

private fun testIPv4Address(hostAddress: String = "10.0.0.1", prefixLength: Int = 32): IPAddress.V4 =
    IPAddress.V4(
        hostAddress = hostAddress,
        prefixLength = prefixLength,
        inetAddress = Inet4Address.getByName(hostAddress) as Inet4Address
    )

private fun testIPv6Address(hostAddress: String, prefixLength: Int = 64): IPAddress.V6 =
    IPAddress.V6(
        hostAddress = hostAddress,
        prefixLength = prefixLength,
        inetAddress = Inet6Address.getByName(hostAddress) as Inet6Address
    )
