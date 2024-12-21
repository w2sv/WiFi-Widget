package com.w2sv.networking.model

import android.net.LinkAddress
import java.net.Inet4Address
import java.net.Inet6Address
import java.net.InetAddress
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import org.junit.Test
import org.mockito.Mockito

class IPAddressTest {

    @Test
    fun `test fromLinkAddress`() {
        val address = "255.0.0.0"
        val prefixLength = 16

        val ipAddress = IPAddress.fromLinkAddress(linkAddressMock(address, prefixLength))

        assertEquals(IPAddress.Version.V4, ipAddress.version)
        assertEquals(address, ipAddress.hostAddressRepresentation)
        assertEquals(prefixLength, ipAddress.prefixLength)
    }

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
    fun `test isSiteLocal`() {
        listOf(
            "192.168.1.1" to true,
            "10.0.0.1" to true,
            "172.16.0.1" to true,
            "8.8.8.8" to false, // Public
            "1.1.1.1" to false, // Public
            "203.0.113.1" to false // Public
        ).forEach { (address, expected) ->
            assertEquals(expected, testIPAddressFromLinkAddress(address, 24).isSiteLocal)
        }
    }

    @Test
    fun `test isLinkLocal`() {
        listOf(
            "169.254.1.1" to true,
            "169.254.0.0" to true,
            "169.254.255.255" to true,
            "192.168.1.1" to false, // Private
            "8.8.8.8" to false, // Public
            "1.1.1.1" to false // Public
        ).forEach { (address, expected) ->
            assertEquals(expected, testIPAddressFromLinkAddress(address, 24).isLinkLocal)
        }
    }

    @Test
    fun `test isAnyLocal`() {
        listOf(
            "::" to true, // Any Local
            "::1" to false, // Loopback
            "fe80::1" to false, // Link-local
            "2001:db8::1" to false, // Global Unicast
            "ff00::1" to false, // Multicast
            "fc00::1" to false // Unique-local
        ).forEach { (address, expected) ->
            assertEquals(expected, testIPv6Address(address).isAnyLocal)
        }
    }

    @Test
    fun `test isMulticast`() {
        listOf(
            "ff00::1" to true, // Multicast
            "ff02::1" to true, // Multicast
            "ff05::1" to true, // Multicast
            "2001:db8::1" to false, // Global Unicast
            "fe80::1" to false, // Link-local
            "::1" to false // Loopback
        ).forEach { (address, expected) ->
            assertEquals(expected, testIPv6Address(address).isMulticast)
        }
    }

    @Test
    fun `test isLoopback`() {
        listOf(
            "::1" to true, // Loopback
            "127.0.0.1" to true, // IPv4 Loopback
            "2001:db8::1" to false, // Global Unicast
            "fe80::1" to false, // Link-local
            "fc00::1" to false, // Unique-local
            "ff00::1" to false // Multicast
        ).forEach { (address, expected) ->
            assertEquals(expected, testIPAddressFromInetAddress(address).isLoopback)
        }
    }

    @Test
    fun `subnetMask should return correct mask`() {
        listOf(
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
            .forEach { (prefixLength, expectedMask) ->
                assertEquals(expectedMask, testIPv4Address(prefixLength = prefixLength).subnetMask)
            }
    }

    @Test
    fun `isUniqueLocal should return true for unique local addresses and false for non-unique local addresses`() {
        listOf(
            "fc00::1" to true,
            "fd12:3456:789a:1::1" to true,
            "fdff:ffff:ffff:ffff:ffff:ffff:ffff:ffff" to true,
            "2001:db8::1" to false, // Global Unicast
            "fe80::1" to false, // Link-local
            "ff00::1" to false, // Multicast
            "::1" to false, // Loopback
            "2001::1" to false // Global Unicast
        )
            .forEach { (address, expected) ->
                assertEquals(expected, testIPv6Address(address).isUniqueLocal)
            }
    }

    @Test
    fun `isGlobalUnicast should correctly identify global unicast addresses`() {
        listOf(
            // True cases
            "2001:db8:85a3::8a2e:370:7334" to true,
            "2001:0db8:85a3:0000:0000:8a2e:0370:7334" to true,
            "2400:cb00:2049:1::a29f" to true,
            "2a00:1450:4001:81f::2004" to true,
            "2607:f8b0:4004:803::200e" to true,
            "2001:4860:4860::8888" to true,
            // False cases
            "fe80::1" to false, // Link-local
            "fec0::1" to false, // Site-local
            "fd00::1" to false, // Unique local
            "ff02::1" to false, // Multicast
            "::1" to false, // Loopback
            "::" to false // Any local
        )
            .forEach { (hostAddress, expected) ->
                assertEquals(expected, testIPv6Address(hostAddress).isGlobalUnicast)
            }
    }
}

private fun testIPAddressFromInetAddress(address: String): IPAddress =
    IPAddress.fromInetAddress(InetAddress.getByName(address), null)

private fun testIPAddressFromLinkAddress(address: String, prefixLength: Int): IPAddress =
    IPAddress.fromLinkAddress(linkAddressMock(address, prefixLength))

private fun linkAddressMock(address: String, prefixLength: Int): LinkAddress {
    val linkAddress = Mockito.mock(LinkAddress::class.java)

    // Stub methods to return desired values
    Mockito.`when`(linkAddress.address).thenReturn(InetAddress.getByName(address))
    Mockito.`when`(linkAddress.prefixLength).thenReturn(prefixLength)

    return linkAddress
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
