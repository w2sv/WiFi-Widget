package com.w2sv.networking.remotenetworkinfo

import com.w2sv.kotlinutils.coroutines.runCatchingCancellable
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.coroutines.executeAsync
import slimber.log.e

/**
 * Executes a cancellable GET request and maps the response body using [transform].
 */
internal suspend fun <T> OkHttpClient.fetchFromUrl(url: String, transform: (String) -> T): Result<T> =
    withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url(url)
            .build()

        runCatchingCancellable {
            newCall(request)
                .executeAsync()
                .use { response ->
                    if (!response.isSuccessful) {
                        throw IOException("Unexpected code $response")
                    }
                    transform(response.body.string())
                }
        }.onFailure { t -> e { "Fetch failed for $url: $t" } }
    }
