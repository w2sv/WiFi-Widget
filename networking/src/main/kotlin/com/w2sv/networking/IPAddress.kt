package com.w2sv.networking

import android.net.ConnectivityManager
import com.w2sv.domain.model.IPAddress
import okhttp3.OkHttpClient
import okhttp3.Request
import okio.IOException
import slimber.log.i
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

fun getPublicIPAddress(httpClient: OkHttpClient): String? {
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
