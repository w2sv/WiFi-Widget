package com.w2sv.networking

import android.net.LinkAddress
import androidx.annotation.IntRange
import com.w2sv.common.utils.removeAlphanumeric

data class IPAddress(
    @IntRange(from = 0, to = 128) val prefixLength: Int,
    private val hostAddress: String?,
    val isLinkLocal: Boolean,
    val isSiteLocal: Boolean,
    val isAnyLocal: Boolean,
    val isLoopback: Boolean,
    val isMulticast: Boolean,
) {
    constructor(linkAddress: LinkAddress) : this(
        prefixLength = linkAddress.prefixLength,
        hostAddress = linkAddress.address.hostAddress,
        isLinkLocal = linkAddress.address.isLinkLocalAddress,
        isSiteLocal = linkAddress.address.isSiteLocalAddress,
        isAnyLocal = linkAddress.address.isAnyLocalAddress,
        isLoopback = linkAddress.address.isLoopbackAddress,
        isMulticast = linkAddress.address.isMulticastAddress,
    )

//    /**
//     * Reference: https://stackoverflow.com/a/33094601/12083276
//     */
//    fun getNetmask(): String {
//        val shift = 0xffffffff shl (32 - prefixLength)
//        return "${((shift and 0xff000000) shr 24) and 0xff}" +
//                ".${((shift and 0x00ff0000) shr 16) and 0xff}" +
//                ".${((shift and 0x0000ff00) shr 8) and 0xff}" +
//                ".${(shift and 0x000000ff) and 0xff}"
//    }

    val version: Version = if (prefixLength < Version.V6.minPrefixLength) Version.V4 else Version.V6
    val hostAddressRepresentation: String = hostAddress ?: version.fallbackAddress

    private val isLocal: Boolean
        get() = isSiteLocal || isLinkLocal || isAnyLocal

    /**
     * @see <a href="https://networklessons.com/ipv6/ipv6-address-types">reference</a>
     */
    val isUniqueLocal: Boolean
        get() = hostAddressRepresentation.run { startsWith("fc") || startsWith("fd") }

    val isGlobalUnicast: Boolean
        get() = !isLocal && !isMulticast

    enum class Version(
        val minPrefixLength: Int,
        val fallbackAddress: String,
        val ofCorrectFormat: (String) -> Boolean
    ) {
        V4(0, "0.0.0.0", { it.removeAlphanumeric() == "..." }),
        V6(64, ":::::::", { it.removeAlphanumeric() == ":::::::" }),
    }
}