package com.w2sv.networking

import android.net.LinkAddress
import androidx.annotation.IntRange

data class IPAddress(
    @IntRange(from = 0, to = 128) val prefixLength: Int,
    private val hostAddress: String?,
    val isLinkLocal: Boolean,
    val isSiteLocal: Boolean,
    val isAnyLocal: Boolean,
    val isLoopback: Boolean,
    val isMulticast: Boolean,
    val isMCGlobal: Boolean,
    val isMCLinkLocal: Boolean,
    val isMCSiteLocal: Boolean,
    val isMCNodeLocal: Boolean,
    val isMCOrgLocal: Boolean
) {
    constructor(linkAddress: LinkAddress) : this(
        prefixLength = linkAddress.prefixLength,
        hostAddress = linkAddress.address.hostAddress,
        isLinkLocal = linkAddress.address.isLinkLocalAddress,
        isSiteLocal = linkAddress.address.isSiteLocalAddress,
        isAnyLocal = linkAddress.address.isAnyLocalAddress,
        isLoopback = linkAddress.address.isLoopbackAddress,
        isMulticast = linkAddress.address.isMulticastAddress,
        isMCGlobal = linkAddress.address.isMCGlobal,
        isMCLinkLocal = linkAddress.address.isMCLinkLocal,
        isMCSiteLocal = linkAddress.address.isMCSiteLocal,
        isMCNodeLocal = linkAddress.address.isMCNodeLocal,
        isMCOrgLocal = linkAddress.address.isMCOrgLocal,
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
        get() = !isSiteLocal && !isLinkLocal && !isAnyLocal && !isMulticast

    enum class Type(
        val minPrefixLength: Int,
        val fallbackAddress: String,
        val ofCorrectFormat: (String) -> Boolean
    ) {
        V4(0, "0.0.0.0", { it.removeAlphanumeric() == "..." }),
        V6(64, ":::::::", { it.removeAlphanumeric() == ":::::::" }),
    }
}

private fun String.removeAlphanumeric(): String =
    replace(Regex("\\w"), "")