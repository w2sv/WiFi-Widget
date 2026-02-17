package com.w2sv.domain.model

import androidx.annotation.IntRange

/**
 * Represents an IP address in the system or public internet.
 *
 * @property version Whether this is an IPv4 or IPv6 address.
 * @property hostAddress The IP address string, or `null` if unknown.
 * @property prefixLength The network prefix length, or `null` if unknown.
 */
sealed class IpAddress(val version: Version) {
    abstract val hostAddress: String?
    abstract val prefixLength: Int?
    abstract val isSiteLocal: Boolean
    abstract val isLinkLocal: Boolean
    abstract val isAnyLocal: Boolean
    abstract val isMulticast: Boolean
    abstract val isLoopback: Boolean

    /**
     * Returns the string representation of the IP address, or a fallback address
     * defined per version if unknown.
     */
    val hostAddressRepresentation: String
        get() = hostAddress ?: version.fallbackAddress

    val asV4OrNull: V4?
        get() = this as? V4

    val asV6OrNull: V6?
        get() = this as? V6

    /**
     * The IP address version.
     */
    enum class Version(val fallbackAddress: String) {
        V4("0.0.0.0"),
        V6(":::::::")
    }

    /**
     * Represents an IPv4 address with additional derived properties.
     *
     * @property subnetMask The subnet mask derived from the prefix length, or `null`.
     * @property isSiteLocal True if the IP is a site-local address (private IPv4).
     */
    data class V4(
        override val hostAddress: String?,
        @IntRange(from = 0, to = 32)
        override val prefixLength: Int?,
        val subnetMask: String?,
        override val isSiteLocal: Boolean,
        override val isLinkLocal: Boolean,
        override val isAnyLocal: Boolean,
        override val isMulticast: Boolean,
        override val isLoopback: Boolean
    ) : IpAddress(Version.V4)

    /**
     * Represents an IPv6 address with additional derived properties.
     *
     * @property isUniqueLocal True if the address is within the unique local range (fc00::/7).
     * @property isGlobalUnicast True if the address is a global unicast address (2000::/3).
     */
    data class V6(
        override val hostAddress: String?,
        @IntRange(from = 0, to = 128)
        override val prefixLength: Int?,
        override val isSiteLocal: Boolean,
        override val isLinkLocal: Boolean,
        override val isAnyLocal: Boolean,
        override val isMulticast: Boolean,
        override val isLoopback: Boolean,
        val isUniqueLocal: Boolean,
        val isGlobalUnicast: Boolean
    ) : IpAddress(Version.V6)
}
