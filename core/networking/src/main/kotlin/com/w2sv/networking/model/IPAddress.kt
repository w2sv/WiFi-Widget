package com.w2sv.networking.model

import android.net.LinkAddress
import androidx.annotation.IntRange
import com.w2sv.common.utils.log
import com.w2sv.common.utils.removeAlphanumeric
import com.w2sv.networking.extensions.fetchFromUrl
import okhttp3.OkHttpClient
import slimber.log.i
import java.io.IOException
import java.net.Inet4Address
import java.net.Inet6Address

internal sealed class IPAddress(val version: Version) {
    protected abstract val hostAddress: String?
    abstract val prefixLength: Int?
    abstract val isLinkLocal: Boolean
    abstract val isSiteLocal: Boolean
    abstract val isAnyLocal: Boolean
    abstract val isLoopback: Boolean
    abstract val isMulticast: Boolean

    val hostAddressRepresentation: String = hostAddress ?: version.fallbackAddress

    protected abstract val isLocal: Boolean

    val asV4OrNull: V4?
        get() = this as? V4

    val asV6OrNull: V6?
        get() = this as? V6

    enum class Version(
        val fallbackAddress: String,
        val ofCorrectFormat: (String) -> Boolean
    ) {
        V4("0.0.0.0", { it.removeAlphanumeric() == "..." }),
        V6(":::::::", { it.removeAlphanumeric() == ":::::::" })
    }

    data class V4(
        @IntRange(from = 0, to = 32)
        override val prefixLength: Int?,
        override val hostAddress: String?,
        override val isLinkLocal: Boolean,
        override val isSiteLocal: Boolean,
        override val isAnyLocal: Boolean,
        override val isLoopback: Boolean,
        override val isMulticast: Boolean
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

        override val isLocal: Boolean
            get() = isSiteLocal || isLinkLocal || isAnyLocal
    }

    data class V6(
        @IntRange(from = 0, to = 128)
        override val prefixLength: Int?,
        override val hostAddress: String?,
        override val isLinkLocal: Boolean,
        override val isSiteLocal: Boolean,
        override val isAnyLocal: Boolean,
        override val isLoopback: Boolean,
        override val isMulticast: Boolean
    ) : IPAddress(Version.V6) {

        /**
         * @see <a href="https://networklessons.com/ipv6/ipv6-address-types">reference</a>
         * @see <a href="https://datatracker.ietf.org/doc/html/rfc4193">reference</a>
         */
        val isUniqueLocal: Boolean
            get() = ulaIdentificationRegex.matches(hostAddressRepresentation.substring(0, 2))

        val isGlobalUnicast: Boolean
            get() = !isLocal && !isMulticast

        override val isLocal: Boolean
            get() = isSiteLocal || isLinkLocal || isAnyLocal || isUniqueLocal

        companion object {
            private val ulaIdentificationRegex = Regex("^(fc|fd)")
        }
    }

    companion object {
        fun fromLinkAddress(linkAddress: LinkAddress): IPAddress =
            when (linkAddress.address) {
                is Inet4Address -> {
                    V4(
                        hostAddress = linkAddress.address.hostAddress,
                        prefixLength = linkAddress.prefixLength.also { require(it <= 32) },
                        isLinkLocal = linkAddress.address.isLinkLocalAddress,
                        isSiteLocal = linkAddress.address.isSiteLocalAddress,
                        isAnyLocal = linkAddress.address.isAnyLocalAddress,
                        isLoopback = linkAddress.address.isLoopbackAddress,
                        isMulticast = linkAddress.address.isMulticastAddress
                    )
                }

                is Inet6Address -> {
                    V6(
                        hostAddress = linkAddress.address.hostAddress,
                        prefixLength = linkAddress.prefixLength,
                        isLinkLocal = linkAddress.address.isLinkLocalAddress,
                        isSiteLocal = linkAddress.address.isSiteLocalAddress,
                        isAnyLocal = linkAddress.address.isAnyLocalAddress,
                        isLoopback = linkAddress.address.isLoopbackAddress,
                        isMulticast = linkAddress.address.isMulticastAddress
                    )
                }

                else -> error("Invalid InetAddress child.")
            }

        suspend fun fetchPublic(httpClient: OkHttpClient, version: Version): Result<IPAddress> {
            i { "Fetching public $version address" }
            return httpClient.fetchFromUrl(
                when (version) {
                    Version.V4 -> "https://api.ipify.org"
                    Version.V6 -> "https://api6.ipify.org"
                }
            ) { address ->
                i { "Got public $version address $address" }
                if (version.ofCorrectFormat(address)) {
                    when (version) {
                        Version.V4 -> V4(
                            hostAddress = address,
                            prefixLength = null,
                            isLinkLocal = false,
                            isSiteLocal = false,
                            isAnyLocal = false,
                            isLoopback = false,
                            isMulticast = false
                        )
                        Version.V6 -> V6(
                            hostAddress = address,
                            prefixLength = null,
                            isLinkLocal = false,
                            isSiteLocal = false,
                            isAnyLocal = false,
                            isLoopback = false,
                            isMulticast = false
                        )
                    }
                } else {
                    throw IOException("Obtained $version address $address of incorrect format")
                }
            }
        }
    }
}
