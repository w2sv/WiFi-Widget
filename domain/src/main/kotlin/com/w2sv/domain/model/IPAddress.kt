package com.w2sv.domain.model

import android.net.LinkAddress
import androidx.annotation.IntRange
import java.net.InetAddress

data class IPAddress(
    @IntRange(from = 0, to = 128)
    val prefixLength: Int,
    private val hostAddress: String?,
    val localAttributes: LocalAttributes,
    val isLoopback: Boolean,
    val isMulticast: Boolean,
) {
    constructor(linkAddress: LinkAddress) : this(
        linkAddress.prefixLength,
        linkAddress.address.hostAddress,
        LocalAttributes(linkAddress.address),
        linkAddress.address.isLoopbackAddress,
        linkAddress.address.isMulticastAddress,
    )

    /**
     * Reference: https://stackoverflow.com/a/33094601/12083276
     */
    fun getNetmask(): String {
        val shift = 0xffffffff shl (32 - prefixLength)
        return "${((shift and 0xff000000) shr 24) and 0xff}" +
                ".${((shift and 0x00ff0000) shr 16) and 0xff}" +
                ".${((shift and 0x0000ff00) shr 8) and 0xff}" +
                ".${(shift and 0x000000ff) and 0xff}"
    }

    val type: Type = if (prefixLength < Type.V6.minPrefixLength) Type.V4 else Type.V6
    val hostAddressRepresentation: String = hostAddress ?: type.fallbackAddress

    val isUniqueLocal: Boolean
        get() = hostAddressRepresentation.startsWith("fc00::/7")

    val isGlobalUnicast: Boolean
        get() = hostAddressRepresentation.startsWith("2000::/3")

    enum class Type(val minPrefixLength: Int, val fallbackAddress: String) {
        V4(0, "0.0.0.0"),
        V6(64, "::::::"),
    }

    data class LocalAttributes(
        val linkLocal: Boolean,
        val siteLocal: Boolean,
        val anyLocal: Boolean,
    ) {
        constructor(inetAddress: InetAddress) : this(
            linkLocal = inetAddress.isLinkLocalAddress,
            siteLocal = inetAddress.isSiteLocalAddress,
            anyLocal = inetAddress.isAnyLocalAddress,
        )
    }
}