package com.w2sv.networking.model

import android.net.ConnectivityManager
import android.net.LinkAddress
import androidx.annotation.IntRange
import androidx.annotation.VisibleForTesting
import com.w2sv.common.utils.log
import com.w2sv.networking.extensions.fetchFromUrl
import com.w2sv.networking.extensions.linkProperties
import java.io.IOException
import java.net.Inet4Address
import java.net.Inet6Address
import java.net.InetAddress
import okhttp3.OkHttpClient
import slimber.log.i

internal sealed class IPAddress(val version: Version) {
    protected abstract val hostAddress: String?
    abstract val prefixLength: Int?
    protected abstract val inetAddress: InetAddress

    val hostAddressRepresentation: String
        get() = hostAddress ?: version.fallbackAddress

    /**
     * @see InetAddress.isSiteLocalAddress
     */
    val isSiteLocal get() = inetAddress.isSiteLocalAddress

    /**
     * @see InetAddress.isLinkLocalAddress
     */
    val isLinkLocal get() = inetAddress.isLinkLocalAddress

    /**
     * @see InetAddress.isAnyLocalAddress
     */
    val isAnyLocal get() = inetAddress.isAnyLocalAddress

    /**
     * @see InetAddress.isMulticastAddress
     */
    val isMulticast get() = inetAddress.isMulticastAddress

    /**
     * @see InetAddress.isLoopbackAddress
     */
    val isLoopback get() = inetAddress.isLoopbackAddress

    val asV4OrNull: V4?
        get() = this as? V4

    val asV6OrNull: V6?
        get() = this as? V6

    enum class Version(val fallbackAddress: String, val publicAddressFetchUrl: String) {
        V4("0.0.0.0", "https://api.ipify.org"),
        V6(":::::::", "https://api6.ipify.org")
    }

    data class V4(
        @all:IntRange(from = 0, to = 32)
        override val prefixLength: Int?,
        override val hostAddress: String?,
        override val inetAddress: Inet4Address
    ) : IPAddress(Version.V4) {

        /**
         * @see <a href="https://stackoverflow.com/a/33094601/12083276">SO reference</a>
         */
        val subnetMask: String? by lazy {
            prefixLength?.let {
                val shift = 0xffffffff shl (32 - it)
                "${((shift and 0xff000000) shr 24) and 0xff}" +
                    ".${((shift and 0x00ff0000) shr 16) and 0xff}" +
                    ".${((shift and 0x0000ff00) shr 8) and 0xff}" +
                    ".${(shift and 0x000000ff) and 0xff}"
            }
        }
    }

    data class V6(
        @all:IntRange(from = 0, to = 128)
        override val prefixLength: Int?,
        override val hostAddress: String?,
        override val inetAddress: Inet6Address
    ) : IPAddress(Version.V6) {

        /**
         * Taken from [ConnectivityUtils](https://cs.android.com/android/platform/superproject/+/android14-qpr3-release:packages/modules/Connectivity/staticlibs/framework/com/android/net/module/util/ConnectivityUtils.java?q=ConnectivityUtils.isIPv6ULA&ss=android%2Fplatform%2Fsuperproject),
         * which is a hidden Android API, found through the private function [LinkAddress].isIpv6ULA.
         *
         * Per [RFC 4193 section 8](https://datatracker.ietf.org/doc/html/rfc4193#section-8), fc00::/ 7 identifies these addresses
         *
         * Further references:
         * @see <a href="https://networklessons.com/ipv6/ipv6-address-types#Unique_Local">reference</a>
         */
        val isUniqueLocal: Boolean
            get() = (inetAddress.firstByte and 0xfe) == 0xfc

        /**
         * As per [RFC 4291 section 2.5.4](https://datatracker.ietf.org/doc/html/rfc4291#section-2.5.4) & [RFC 3513](https://datatracker.ietf.org/doc/html/rfc3513#section-4)
         * Addresses matching the range 2000::/3 (-> binary prefix of 001)
         *
         * Further references:
         * [Reddit](https://www.reddit.com/r/ipv6/comments/u7aqxg/what_if_20003_gets_extended/?rdt=54218)
         */
        val isGlobalUnicast: Boolean
            get() = (inetAddress.firstByte and 0xff and 0xe0) == 0x20 // Matches the range 2000::/3
    }

    companion object {
        fun systemAddresses(connectivityManager: ConnectivityManager): List<IPAddress> =
            connectivityManager.linkProperties?.linkAddresses?.map(::fromLinkAddress).log { "IP Addresses: $it" } ?: emptyList()

        @VisibleForTesting
        fun fromLinkAddress(linkAddress: LinkAddress): IPAddress =
            fromInetAddress(linkAddress.address, linkAddress.prefixLength)

        /**
         * Fetches the public IP address string from the respective [Version.publicAddressFetchUrl] and parses it via [InetAddress.getByName].
         *
         * @return a [Result] wrapping either
         * - [com.w2sv.networking.model.IPAddress] for valid address strings matching the [version]
         * - [java.net.UnknownHostException] if the retrieved address is invalid
         * - [IOException] if the parsed [com.w2sv.networking.model.IPAddress] type doesn't match [version]
         * - an exception thrown by [fetchFromUrl]
         */
        suspend fun fetchPublic(httpClient: OkHttpClient, version: Version): Result<IPAddress> {
            i { "Fetching public $version address" }
            return httpClient.fetchFromUrl(version.publicAddressFetchUrl) { address ->
                i { "Got public $version address $address" }
                fromInetAddress(address = InetAddress.getByName(address), prefixLength = null)
                    .also {
                        if (it.version != version) {
                            throw IOException("Obtained $version address $address of incorrect format")
                        }
                    }
            }
        }

        @VisibleForTesting
        fun fromInetAddress(address: InetAddress, prefixLength: Int?): IPAddress =
            when (address) {
                is Inet4Address -> {
                    V4(
                        hostAddress = address.hostAddress,
                        prefixLength = prefixLength.also { require(it == null || it <= 32) },
                        inetAddress = address
                    )
                }

                is Inet6Address -> {
                    V6(
                        hostAddress = address.hostAddress,
                        prefixLength = prefixLength,
                        inetAddress = address
                    )
                }

                else -> error("Invalid InetAddress child.")
            }
    }
}

private val InetAddress.firstByte: Int
    get() = address.first().toInt()
