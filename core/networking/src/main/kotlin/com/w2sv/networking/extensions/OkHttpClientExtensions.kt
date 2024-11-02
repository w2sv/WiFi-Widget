package com.w2sv.networking.extensions

import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout
import okhttp3.OkHttpClient
import okhttp3.Request
import slimber.log.e
import java.io.IOException
import java.net.SocketTimeoutException

internal suspend fun <T> OkHttpClient.fetchFromUrl(
    url: String,
    timeout: Long = 5_000,
    onSuccess: (String) -> T
): Result<T> {
    val request = Request.Builder()
        .url(url)
        .build()

    return try {
        withTimeout(timeout) {
            newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    throw IOException("Unexpected code $response")
                }
                response
                    .body
                    ?.string()
                    ?.let { Result.success(onSuccess(it)) }
                    ?: throw IOException("Empty response body")
            }
        }
    } catch (e: Exception) {
        when (e) {
            is SocketTimeoutException, is TimeoutCancellationException -> e { "Timed out with ${e.message} trying to fetch data from $url" }
            else -> {
                e { "Received ${e.message} when trying to fetch data from $url" }
                e.printStackTrace()
            }
        }
        Result.failure(e)
    }
}