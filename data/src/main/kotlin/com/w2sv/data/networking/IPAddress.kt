package com.w2sv.data.networking

import android.net.ConnectivityManager
import android.net.LinkAddress
import androidx.annotation.IntRange
import java.net.InetAddress
import java.nio.ByteBuffer
import java.nio.ByteOrder

class IPAddress(linkAddress: LinkAddress) {
    @IntRange(from = 0, to = 128)
    val prefixLength: Int = linkAddress.prefixLength

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

    val textualRepresentation: String = linkAddress.address.hostAddress ?: type.fallbackAddress

    val localAttributes: LocalAttributes = LocalAttributes.get(linkAddress.address)
    val isLocal: Boolean get() = localAttributes.properties.any { it }

    val isLoopback = linkAddress.address.isLoopbackAddress

    val multiCastAttributes: MultiCastAttributes? = MultiCastAttributes.get(linkAddress.address)
    val isMultiCast: Boolean get() = multiCastAttributes != null

    enum class Type(val fallbackAddress: String) {
        V4("0.0.0.0"),
        V6("::::::")
    }

    data class LocalAttributes(
        val linkLocal: Boolean,
        val siteLocal: Boolean,
        val anyLocal: Boolean
    ) {
        val properties: List<Boolean> get() = listOf(linkLocal, siteLocal, anyLocal)

        companion object {
            fun get(inetAddress: InetAddress): LocalAttributes =
                LocalAttributes(
                    inetAddress.isLinkLocalAddress,
                    inetAddress.isSiteLocalAddress,
                    inetAddress.isAnyLocalAddress
                )
        }
    }

    data class MultiCastAttributes(
        val global: Boolean,
        val linkLocal: Boolean,
        val nodeLocal: Boolean,
        val orgLocal: Boolean,
        val siteLocal: Boolean
    ) {
        companion object {
            fun get(inetAddress: InetAddress): MultiCastAttributes? =
                if (inetAddress.isMulticastAddress)
                    MultiCastAttributes(
                        inetAddress.isMCGlobal,
                        inetAddress.isMCLinkLocal,
                        inetAddress.isMCNodeLocal,
                        inetAddress.isMCOrgLocal,
                        inetAddress.isMCSiteLocal
                    )
                else null
        }
    }
}

fun ConnectivityManager.getIPAddresses(): List<IPAddress>? =
    linkProperties?.linkAddresses?.map { IPAddress(it) }

/**
 * Reference: https://stackoverflow.com/a/52663352/12083276
 */
fun textualIPv4Representation(address: Int): String? =
    InetAddress.getByAddress(
        ByteBuffer
            .allocate(Integer.BYTES)
            .order(ByteOrder.LITTLE_ENDIAN)
            .putInt(address)
            .array()
    )
        .hostAddress