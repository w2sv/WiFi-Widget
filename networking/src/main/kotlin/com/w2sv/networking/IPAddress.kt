package com.w2sv.networking

import android.net.ConnectivityManager
import com.w2sv.domain.model.IPAddress
import java.net.InetAddress
import java.nio.ByteBuffer
import java.nio.ByteOrder

fun ConnectivityManager.getIPAddresses(): List<IPAddress> =
    linkProperties?.linkAddresses?.map { IPAddress(it) } ?: listOf()

/**
 * Reference: https://stackoverflow.com/a/52663352/12083276
 */
fun textualIPv4Representation(address: Int): String? =
    InetAddress.getByAddress(
        ByteBuffer
            .allocate(Integer.BYTES)
            .order(ByteOrder.LITTLE_ENDIAN)
            .putInt(address)
            .array(),
    )
        .hostAddress