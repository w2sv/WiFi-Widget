package com.w2sv.networking

import android.net.LinkAddress
import com.w2sv.domain.model.networking.IpAddress
import io.mockk.every
import io.mockk.mockk
import java.net.Inet4Address
import java.net.InetAddress
import kotlin.test.assertEquals
import org.junit.Test

class IpAddressConversionKtTest {

    @Test
    fun fromLinkAddress() {
        val host = "255.0.0.0"
        val prefixLength = 16

        val address = linkAddressMock(host, prefixLength).toIpAddress()

        assertEquals(IpAddress.Version.V4, address.version)
        assertEquals(host, address.hostAddressRepresentation)
        assertEquals(prefixLength, address.prefixLength)
    }

    @Test
    fun isSiteLocal() {
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
    fun isLinkLocal() {
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
    fun isAnyLocal() {
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
    fun isMulticast() {
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
    fun isLoopback() {
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
    fun `subnetMask conversion from prefix lengths`() {
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
    fun isUniqueLocal() {
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
    fun isGlobalUnicast() {
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

private fun testIPAddressFromInetAddress(address: String): IpAddress =
    InetAddress.getByName(address).toDomain(null)

private fun testIPAddressFromLinkAddress(address: String, prefixLength: Int): IpAddress =
    linkAddressMock(address, prefixLength).toIpAddress()

private fun linkAddressMock(address: String, prefixLength: Int): LinkAddress =
    mockk<LinkAddress>(relaxed = true) {
        every { this@mockk.address } returns InetAddress.getByName(address)
        every { this@mockk.prefixLength } returns prefixLength
    }

private fun testIPv4Address(hostAddress: String = "10.0.0.1", prefixLength: Int = 32): IpAddress.V4 =
    Inet4Address.getByName(hostAddress).toDomain(prefixLength) as IpAddress.V4

private fun testIPv6Address(hostAddress: String, prefixLength: Int = 64): IpAddress.V6 =
    Inet4Address.getByName(hostAddress).toDomain(prefixLength) as IpAddress.V6
