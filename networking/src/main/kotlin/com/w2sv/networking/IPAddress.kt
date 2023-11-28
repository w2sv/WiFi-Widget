package com.w2sv.networking

import okhttp3.OkHttpClient
import okhttp3.Request
import okio.IOException
import slimber.log.i
import java.net.InetAddress
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Reference: https://stackoverflow.com/a/52663352/12083276
 */
internal fun textualIPv4Representation(address: Int): String? =
    InetAddress.getByAddress(
        ByteBuffer
            .allocate(Integer.BYTES)
            .order(ByteOrder.LITTLE_ENDIAN)
            .putInt(address)
            .array(),
    )
        .hostAddress

internal fun getPublicIPAddress(httpClient: OkHttpClient): String? {
    i { "Getting public address" }
    val request = Request.Builder()
        .url("https://api.ipify.org")
        .build()

    try {
        val response = httpClient.newCall(request).execute()
        return response.body?.string().also { i { "Got public address" } }
    } catch (_: IOException) {
        i { "getPublicIPAddress.exception" }
    }

    return null
}
