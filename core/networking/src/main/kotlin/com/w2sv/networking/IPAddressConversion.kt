package com.w2sv.networking

import android.net.ConnectivityManager
import android.net.LinkAddress
import androidx.annotation.IntRange
import androidx.annotation.VisibleForTesting
import com.w2sv.common.utils.log
import com.w2sv.domain.model.networking.IpAddress
import java.net.Inet4Address
import java.net.Inet6Address
import java.net.InetAddress

internal fun ConnectivityManager.systemIpAddresses(): List<IpAddress> =
    linkProperties?.linkAddresses
        ?.map { it.toIpAddress() }
        .log { "IP Addresses: $it" }
        ?: emptyList()

@VisibleForTesting
internal fun LinkAddress.toIpAddress(): IpAddress =
    address.toDomain(prefixLength)

internal fun InetAddress.toDomain(prefixLength: Int?): IpAddress =
    when (this) {
        is Inet4Address -> {
            IpAddress.V4(
                hostAddress = hostAddress,
                prefixLength = prefixLength,
                subnetMask = prefixLength?.let(::subnetMask),
                isSiteLocal = isSiteLocalAddress,
                isLinkLocal = isLinkLocalAddress,
                isAnyLocal = isAnyLocalAddress,
                isMulticast = isMulticastAddress,
                isLoopback = isLoopbackAddress
            )
        }

        is Inet6Address -> {
            IpAddress.V6(
                hostAddress = hostAddress,
                prefixLength = prefixLength,
                isSiteLocal = isSiteLocalAddress,
                isLinkLocal = isLinkLocalAddress,
                isAnyLocal = isAnyLocalAddress,
                isMulticast = isMulticastAddress,
                isLoopback = isLoopbackAddress,
                isUniqueLocal = isUniqueLocal,
                isGlobalUnicast = isGlobalUnicast
            )
        }

        else -> error("Invalid InetAddress child.")
    }

/**
 * @see <a href="https://stackoverflow.com/a/33094601/12083276">SO reference</a>
 */
private fun subnetMask(@IntRange(from = 0, to = 32) prefixLength: Int): String {
    val shift = 0xffffffff shl (32 - prefixLength)
    return "${((shift and 0xff000000) shr 24) and 0xff}" +
        ".${((shift and 0x00ff0000) shr 16) and 0xff}" +
        ".${((shift and 0x0000ff00) shr 8) and 0xff}" +
        ".${(shift and 0x000000ff) and 0xff}"
}

/**
 * Taken from [ConnectivityUtils](https://cs.android.com/android/platform/superproject/+/android14-qpr3-release:packages/modules/Connectivity/staticlibs/framework/com/android/net/module/util/ConnectivityUtils.java?q=ConnectivityUtils.isIPv6ULA&ss=android%2Fplatform%2Fsuperproject),
 * which is a hidden Android API, found through the private function [LinkAddress].isIpv6ULA.
 *
 * Per [RFC 4193 section 8](https://datatracker.ietf.org/doc/html/rfc4193#section-8), fc00::/ 7 identifies these addresses
 *
 * Further references:
 * @see <a href="https://networklessons.com/ipv6/ipv6-address-types#Unique_Local">reference</a>
 */
private val Inet6Address.isUniqueLocal: Boolean
    get() = (firstByte and 0xfe) == 0xfc

/**
 * As per [RFC 4291 section 2.5.4](https://datatracker.ietf.org/doc/html/rfc4291#section-2.5.4) & [RFC 3513](https://datatracker.ietf.org/doc/html/rfc3513#section-4)
 * Addresses matching the range 2000::/3 (-> binary prefix of 001)
 *
 * Further references:
 * [Reddit](https://www.reddit.com/r/ipv6/comments/u7aqxg/what_if_20003_gets_extended/?rdt=54218)
 */
val Inet6Address.isGlobalUnicast: Boolean
    get() = (firstByte and 0xff and 0xe0) == 0x20 // Matches the range 2000::/3

private val InetAddress.firstByte: Int
    get() = address.first().toInt()
