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

    val type: Type = if (prefixLength < 64) Type.V4 else Type.V6
    val hostAddressRepresentation: String = hostAddress ?: type.fallbackAddress

    val isUniqueLocal: Boolean
        get() = hostAddressRepresentation.startsWith("fc00::/7")

    val isGlobalUnicast: Boolean
        get() = hostAddressRepresentation.startsWith("2000::/3")

    enum class Type(val fallbackAddress: String) {
        V4("0.0.0.0"),
        V6("::::::"),
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

//    fun getViewProperties(includePrefixLength: Boolean): Sequence<String> = sequence {
//        if (includePrefixLength) {
//            yield("/$prefixLength")
//        }
//        if (isLocal) {
//            var yieldedSpecificLocalProperty = false
//            if (localAttributes.linkLocal) {
//                yield("LinkLocal")
//                yieldedSpecificLocalProperty = true
//            }
//            if (localAttributes.siteLocal) {
//                yield("SiteLocal")
//                yieldedSpecificLocalProperty = true
//            }
//            if (!yieldedSpecificLocalProperty) {
//                yield("Local")
//            }
//        } else {
//            yield("Public")
//        }
//        if (isMulticast) {
//            yield("Multicast")
//        }
//        if (isLoopback) {
//            yield("Loopback")
//        }
//    }

//    data class MultiCastAttributes(
//        val global: Boolean,
//        val linkLocal: Boolean,
//        val nodeLocal: Boolean,
//        val orgLocal: Boolean,
//        val siteLocal: Boolean
//    ) {
//        companion object {
//            fun get(inetAddress: InetAddress): MultiCastAttributes? =
//                if (inetAddress.isMulticastAddress)
//                    MultiCastAttributes(
//                        inetAddress.isMCGlobal,
//                        inetAddress.isMCLinkLocal,
//                        inetAddress.isMCNodeLocal,
//                        inetAddress.isMCOrgLocal,
//                        inetAddress.isMCSiteLocal
//                    )
//                else null
//        }
//    }
}