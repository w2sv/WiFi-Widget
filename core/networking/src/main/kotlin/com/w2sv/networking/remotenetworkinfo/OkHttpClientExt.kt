package com.w2sv.networking.remotenetworkinfo

import java.io.IOException
import java.net.SocketTimeoutException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import okhttp3.OkHttpClient
import okhttp3.Request
import slimber.log.e

internal suspend fun <T> OkHttpClient.fetchFromUrl(
    url: String,
    timeout: Long = 5_000,
    onSuccess: (String) -> T
): Result<T> =
    withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url(url)
            .build()

        try {
            withTimeout(timeout) {
                newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        throw IOException("Unexpected code $response")
                    }
                    Result.success(onSuccess(response.body.string()))
                }
            }
        } catch (e: Exception) {
            when (e) {
                is SocketTimeoutException, is TimeoutCancellationException -> e {
                    "Timed out with ${e.message} trying to fetch data from $url"
                }
                else -> e { "Received $e when trying to fetch data from $url" }
            }
            Result.failure(e)
        }
    }
