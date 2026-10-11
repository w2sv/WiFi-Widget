package com.w2sv.networking.remotenetworkinfo

import com.w2sv.domain.model.widget.WidgetConfig

internal sealed interface FetchResult<out T> {
    data class Success<T>(val value: T) : FetchResult<T>
    data object Failed : FetchResult<Nothing>
    data object Skipped : FetchResult<Nothing>
}

/** Executes a configured fetch, keeping a skip distinct from a network failure. */
internal abstract class ConditionalFetcher<T> {

    suspend fun fetchIfNecessary(config: WidgetConfig): FetchResult<T> =
        if (shouldFetch(config)) {
            performFetch(config)
        } else {
            FetchResult.Skipped
        }

    protected abstract fun shouldFetch(config: WidgetConfig): Boolean

    protected abstract suspend fun performFetch(config: WidgetConfig): FetchResult<T>
}
