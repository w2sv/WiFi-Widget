package com.w2sv.networking.model

import android.net.LinkAddress
import androidx.annotation.IntRange
import com.w2sv.networking.extensions.fetchFromUrl
import okhttp3.OkHttpClient
import slimber.log.i
import java.io.IOException
import java.net.Inet4Address
import java.net.Inet6Address
import java.net.InetAddress

internal sealed class IPAddress(val version: Version) {
    protected abstract val hostAddress: String?
    abstract val prefixLength: Int?
    protected abstract val inetAddress: InetAddress

    val hostAddressRepresentation: String = hostAddress ?: version.fallbackAddress

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

    enum class Version(val fallbackAddress: String) {
        V4("0.0.0.0"),
        V6(":::::::")
    }

    data class V4(
        @IntRange(from = 0, to = 32)
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
        @IntRange(from = 0, to = 128)
        override val prefixLength: Int?,
        override val hostAddress: String?,
        override val inetAddress: Inet6Address
    ) : IPAddress(Version.V6) {

        /**
         * https://cs.android.com/android/platform/superproject/+/android14-qpr3-release:packages/modules/Connectivity/staticlibs/framework/com/android/net/module/util/ConnectivityUtils.java?q=ConnectivityUtils.isIPv6ULA&ss=android%2Fplatform%2Fsuperproject
         * @see <a href="https://networklessons.com/ipv6/ipv6-address-types">reference</a>
         * @see <a href="https://datatracker.ietf.org/doc/html/rfc4193">reference</a>
         */
        val isUniqueLocal: Boolean
            get() = (inetAddress.address[0].toInt() and 0xfe) == 0xfc

        val isGlobalUnicast: Boolean
            get() = !isLocal && !isMulticast

        private val isLocal: Boolean
            get() = isSiteLocal || isLinkLocal || isAnyLocal || isUniqueLocal
    }

    companion object {
        fun fromLinkAddress(linkAddress: LinkAddress): IPAddress =
            fromInetAddress(linkAddress.address, linkAddress.prefixLength)

        suspend fun fetchPublic(httpClient: OkHttpClient, version: Version): Result<IPAddress> {
            i { "Fetching public $version address" }
            return httpClient.fetchFromUrl(
                when (version) {
                    Version.V4 -> "https://api.ipify.org"
                    Version.V6 -> "https://api6.ipify.org"
                }
            ) { address ->
                i { "Got public $version address $address" }
                fromInetAddress(InetAddress.getByName(address), null)
                    .also {
                        if (it.version != version) {
                            throw IOException("Obtained $version address $address of incorrect format")
                        }
                    }
            }
        }

        private fun fromInetAddress(address: InetAddress, prefixLength: Int?): IPAddress =
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
