package com.w2sv.networking.remotenetworkinfo

import com.w2sv.domain.model.widget.WidgetConfig

/**
 * Abstraction for conditionally executing a network fetch based on [WidgetConfig].
 */
internal abstract class ConditionalFetcher<T> {

    /**
     * Executes [performFetch] if [shouldFetch] returns true, otherwise returns `null`.
     */
    suspend fun fetchIfNecessary(config: WidgetConfig): T? =
        if (shouldFetch(config)) {
            performFetch(config)
        } else {
            null
        }

    /**
     * Determines whether a fetch should be performed for the given [config].
     */
    protected abstract fun shouldFetch(config: WidgetConfig): Boolean

    /**
     * Performs the actual data fetch.
     * @return `null` in the case of an error.
     */
    protected abstract suspend fun performFetch(config: WidgetConfig): T?
}
